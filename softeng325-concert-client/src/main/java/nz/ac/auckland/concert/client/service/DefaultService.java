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

    private Set<String> cookieValues = new HashSet<>();

    private Client client;
    private Response response;

    /**
     * Resets any error message and status code and creates a new client connection.
     */
    private void createNewClientConnection() {
        client = ClientBuilder.newClient();
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
     * @see ConcertService#getConcerts()
     */
    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {
        Set<ConcertDTO> concerts = null;
        try {
            createNewClientConnection();

            Builder builder = client.target(WEB_SERVICE_URI + CONCERTS_URI)
                    .request()
                    .accept(MediaType.APPLICATION_XML);

            addCookieToInvocation(builder);
            response = builder.get();

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                concerts = response.readEntity(new GenericType<Set<ConcertDTO>>() {
                });
            }
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            processCookieThenCheckResponseStatusAndCloseClientConnection();
        }
        return concerts;
    }

    /**
     * Processes the client cookie,
     * then checks the response for error,
     * then closes both connections.
     */
    private void processCookieThenCheckResponseStatusAndCloseClientConnection() throws ServiceException {
        processCookieFromResponse(response);
        checkResponseStatusCodeForError();
        client.close();
        response.close();
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

    private void checkResponseStatusCodeForError() throws ServiceException {
        switch (response.getStatus()) {
            case 400: // BAD_REQUEST
            case 401: // UNAUTHORIZED
            case 404: // NOT_FOUND
            case 500: // INTERNAL_SERVER_ERROR
                throw new ServiceException(response.readEntity(String.class));
        }
    }

    /**
     * @see ConcertService#getPerformers()
     */
    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        Set<PerformerDTO> performers = null;
        try {
            createNewClientConnection();

            Builder builder = client.target(WEB_SERVICE_URI + PERFORMERS_URI)
                    .request()
                    .accept(MediaType.APPLICATION_XML);

            addCookieToInvocation(builder);
            response = builder.get();

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                performers = response.readEntity(new GenericType<Set<PerformerDTO>>() {
                });
            }
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            processCookieThenCheckResponseStatusAndCloseClientConnection();
        }
        return performers;
    }

    /**
     * @see ConcertService#createUser(UserDTO)
     */
    @Override
    public UserDTO createUser(UserDTO newUser) throws ServiceException {
        UserDTO userDTO = null;
        try {
            createNewClientConnection();

            Builder builder = client.target(WEB_SERVICE_URI + USERS_URI)
                    .request();

            addCookieToInvocation(builder);
            response = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                userDTO = response.readEntity(UserDTO.class);
            }
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            processCookieThenCheckResponseStatusAndCloseClientConnection();
        }
        return userDTO;
    }

    /**
     * @see ConcertService#authenticateUser(UserDTO)
     */
    @Override
    public UserDTO authenticateUser(UserDTO user) throws ServiceException {
        UserDTO authenticatedUser = null;
        try {
            createNewClientConnection();

            Builder builder = client.target(WEB_SERVICE_URI + USERS_URI + AUTHENTICATE_URI)
                    .request()
                    .accept(MediaType.APPLICATION_XML);

            addCookieToInvocation(builder);
            response = builder.post(Entity.entity(user, MediaType.APPLICATION_XML));

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                authenticatedUser = response.readEntity(UserDTO.class);
            }
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            processCookieThenCheckResponseStatusAndCloseClientConnection();
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
        try {
            createNewClientConnection();

            Builder builder = client.target(WEB_SERVICE_URI + USERS_URI + RESERVATION_URI)
                    .request()
                    .accept(MediaType.APPLICATION_XML);

            addCookieToInvocation(builder);
            response = builder.post(Entity.entity(reservationRequest, MediaType.APPLICATION_XML));

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                reservation = response.readEntity(ReservationDTO.class);
            }
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            processCookieThenCheckResponseStatusAndCloseClientConnection();
        }
        return reservation;
    }

    @Override
    public void confirmReservation(ReservationDTO reservation) throws ServiceException {
        try {
            createNewClientConnection();
            Builder builder = client.target(WEB_SERVICE_URI + USERS_URI + RESERVATION_URI + CONFIRM_URI)
                    .request();

            addCookieToInvocation(builder);
            response = builder.post(Entity.entity(reservation, MediaType.APPLICATION_XML));

        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            processCookieThenCheckResponseStatusAndCloseClientConnection();
        }
    }

    /**
     * @see ConcertService#registerCreditCard(CreditCardDTO)
     */
    @Override
    public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
        try {
            createNewClientConnection();
            Builder builder = client.target(WEB_SERVICE_URI + USERS_URI + PAYMENT_URI)
                    .request();

            addCookieToInvocation(builder);
            response = builder.post(Entity.entity(creditCard, MediaType.APPLICATION_XML));

        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            processCookieThenCheckResponseStatusAndCloseClientConnection();
        }
    }

    /**
     * @see ConcertService#getBookings()
     */
    @Override
    public Set<BookingDTO> getBookings() throws ServiceException {
        Set<BookingDTO> bookings = null;
        try {
            createNewClientConnection();

            Builder builder = client.target(WEB_SERVICE_URI + USERS_URI + RESERVATION_URI)
                    .request()
                    .accept(MediaType.APPLICATION_XML);

            addCookieToInvocation(builder);
            response = builder.get();

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                bookings = response.readEntity(new GenericType<Set<BookingDTO>>() {
                });
            }
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            processCookieThenCheckResponseStatusAndCloseClientConnection();
        }
        return bookings;
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
