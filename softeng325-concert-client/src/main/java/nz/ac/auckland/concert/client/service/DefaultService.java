package nz.ac.auckland.concert.client.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceException;
import java.awt.*;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static nz.ac.auckland.concert.common.config.CookieConfig.CLIENT_COOKIE;
import static nz.ac.auckland.concert.common.config.URIConfig.*;

public class DefaultService implements ConcertService {

    // AWS S3 access credentials for concert images.
    private static final String AWS_ACCESS_KEY_ID = "AKIAIDYKYWWUZ65WGNJA";
    private static final String AWS_SECRET_ACCESS_KEY = "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";
    // Name of the S3 bucket that stores images.
    private static final String AWS_BUCKET = "concert.aucklanduni.ac.nz";

    private static Set<String> cookieValues = new HashSet<>();

    private Client client;
    private Response response;

    /**
     * Creates a new client connection
     */
    private void createNewClientConnection() {
        client = ClientBuilder.newClient();
    }

    /**
     * Stores any cookie returned in the HTTP response message then closes
     * the response and client connection.
     */
    private void processCookieThenCloseResponseAndClient() {
        processCookieFromResponse(response);
        response.close();
        client.close();
    }

    /**
     * If one exists, adds the previous cookie returned from the Web service to an
     * Invocation.Builder instance.
     */
    private void addCookieToInvocation(Builder builder) {
        if (!cookieValues.isEmpty()) {
            builder.cookie(CLIENT_COOKIE, cookieValues.iterator().next());
        }
    }

    /**
     * Method to extract any cookie from a Response object received from the
     * Web service. If there is a cookie named clientId (Config.CLIENT_COOKIE)
     * it is added to the _cookieValues set, which stores all cookie values for
     * clientId received by the Web service.
     */
    private void processCookieFromResponse(Response response) {
        Map<String, NewCookie> cookies = response.getCookies();

        if (cookies.containsKey(CLIENT_COOKIE)) {
            String cookieValue = cookies.get(CLIENT_COOKIE).getValue();
            cookieValues.add(cookieValue);
        }
    }

    /**
     * @see ConcertService#getConcerts()
     */
    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {
        Set<ConcertDTO> concerts;
        try {
            createNewClientConnection();

            Builder builder = client.target(WEB_SERVICE_URI + CONCERTS_URI)
                    .request()
                    .accept(MediaType.APPLICATION_XML);

            addCookieToInvocation(builder);
            response = builder.get();

            concerts = response.readEntity(new GenericType<Set<ConcertDTO>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            processCookieThenCloseResponseAndClient();
        }
        return concerts;
    }

    /**
     * @see ConcertService#getPerformers()
     */
    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        Set<PerformerDTO> performers;
        try {
            createNewClientConnection();

            Builder builder = client.target(WEB_SERVICE_URI + PERFORMERS_URI)
                    .request()
                    .accept(MediaType.APPLICATION_XML);

            addCookieToInvocation(builder);
            response = builder.get();

            performers = response.readEntity(new GenericType<Set<PerformerDTO>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            processCookieThenCloseResponseAndClient();
        }
        return performers;
    }

    /**
     * @see ConcertService#createUser(UserDTO)
     */
    @Override
    public UserDTO createUser(UserDTO newUser) throws ServiceException {
        int status;
        try {
            createNewClientConnection();

            Builder builder = client.target(WEB_SERVICE_URI + USERS_URI)
                    .request();

            addCookieToInvocation(builder);
            response = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));

            status = response.getStatus();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            processCookieThenCloseResponseAndClient();
        }

        if (status == Response.Status.CONFLICT.getStatusCode()) {
            throw new ServiceException(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME);

        } else if (status == Response.Status.BAD_REQUEST.getStatusCode()) {
            throw new ServiceException(Messages.CREATE_USER_WITH_MISSING_FIELDS);
        }

        return newUser; // "identity property is also set", username is that property...? TODO I guess that means the cookie / Authentication token!!
    }

    /**
     * @see ConcertService#authenticateUser(UserDTO)
     */
    @Override
    public UserDTO authenticateUser(UserDTO user) throws ServiceException {
        UserDTO authenticatedUser = null;
        int status;
        try {
            createNewClientConnection();

            Builder builder = client.target(WEB_SERVICE_URI + USERS_URI + LOGIN_URI)
                    .request()
                    .accept(MediaType.APPLICATION_XML);

            addCookieToInvocation(builder);
            response = builder.post(Entity.entity(user, MediaType.APPLICATION_XML));

            status = response.getStatus();

            if (status == Response.Status.OK.getStatusCode()) {
                authenticatedUser = response.readEntity(UserDTO.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            processCookieThenCloseResponseAndClient();
        }
        if (status == Response.Status.BAD_REQUEST.getStatusCode()) {
            throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS);

        } else if (status == Response.Status.NOT_FOUND.getStatusCode()) {
            throw new ServiceException(Messages.AUTHENTICATE_NON_EXISTENT_USER);

        } else if (status == Response.Status.UNAUTHORIZED.getStatusCode()) {
            throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD);
        }

        return authenticatedUser;
    }

    /**
     * @see ConcertService#getImageForPerformer(PerformerDTO)
     * <p>
     * This method interacts with AWS rather than the concert web service
     * as per the recent Canvas announcement.
     */
    @Override
    public Image getImageForPerformer(PerformerDTO performer) throws ServiceException {
        // Check the performer persists i.e. an image can be retrieved
        if (!getPerformers().contains(performer)) {
            throw new ServiceException(Messages.NO_IMAGE_FOR_PERFORMER);
        }

        String imageName = performer.getImageName();
        File imageFile = new File(imageName);

        TransferManager mgr = null;
        try {
            // Create an AmazonS3 object that represents a connection with the
            // remote S3 service.
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                    AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);

            // Setup the client
            AmazonS3 s3 = AmazonS3ClientBuilder
                    .standard()
                    .withRegion(Regions.AP_SOUTHEAST_2)
                    .withCredentials(
                            new AWSStaticCredentialsProvider(awsCredentials))
                    .build();

            // Setup the transfer
            mgr = TransferManagerBuilder
                    .standard()
                    .withS3Client(s3)
                    .build();

            // Download the image
            Download download = mgr.download(AWS_BUCKET, imageName, imageFile);
            download.waitForCompletion();

        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            if (mgr != null) {
                mgr.shutdownNow();
            }
        }
        return Toolkit.getDefaultToolkit().getImage(String.valueOf(imageFile));
    }

    /**
     * @see ConcertService#reserveSeats(ReservationRequestDTO)
     */
    @Override
    public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {
        ReservationDTO reservation = null;
        int status;
        try {
            createNewClientConnection();

            Builder builder = client.target(WEB_SERVICE_URI + USERS_URI + RESERVATION_URI)
                    .request()
                    .accept(MediaType.APPLICATION_XML);

            addCookieToInvocation(builder);
            response = builder.post(Entity.entity(reservationRequest, MediaType.APPLICATION_XML));

            status = response.getStatus();

            if (status == Response.Status.OK.getStatusCode()) {
                reservation = response.readEntity(ReservationDTO.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            processCookieThenCloseResponseAndClient();
        }

        if (status == Response.Status.BAD_REQUEST.getStatusCode()) {
            throw new ServiceException(Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS);
        }

        return reservation;
    }

    @Override
    public void confirmReservation(ReservationDTO reservation) throws ServiceException {
        // TODO Auto-generated method stub

    }

    @Override
    public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<BookingDTO> getBookings() throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void subscribeForNewsItems(NewsItemListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancelSubscription() {
        throw new UnsupportedOperationException();
    }

}
