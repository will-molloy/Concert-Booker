package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.util.Set;

import static nz.ac.auckland.concert.common.config.URIConfig.CONCERTS_URI;
import static nz.ac.auckland.concert.common.config.URIConfig.PERFORMERS_URI;
import static nz.ac.auckland.concert.common.config.URIConfig.WEB_SERVICE_URI;

public class DefaultService implements ConcertService {

    private Client client;
    private Response response;

    /**
     * Creates a new client connection
     */
    private void createClientConnection() {
        client = ClientBuilder.newClient();
    }

    /**
     * Closes the response and client connections
     */
    private void closeReponseAndClient() {
        response.close();
        client.close();
    }

    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {
        createClientConnection();

        Builder builder = client.target(WEB_SERVICE_URI + CONCERTS_URI).request().accept(MediaType.APPLICATION_XML);
        response = builder.get();

        Set<ConcertDTO> concerts = response
                .readEntity(new GenericType<Set<ConcertDTO>>() {
                });

        closeReponseAndClient();
        return concerts;
    }

    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        createClientConnection();

        Builder builder = client.target(WEB_SERVICE_URI + PERFORMERS_URI).request().accept(MediaType.APPLICATION_XML);
        response = builder.get();

        Set<PerformerDTO> performers = response
                .readEntity(new GenericType<Set<PerformerDTO>>() {
                });

        closeReponseAndClient();
        return performers;
    }

    @Override
    public UserDTO createUser(UserDTO newUser) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserDTO authenticateUser(UserDTO user) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Image getImageForPerformer(PerformerDTO performer) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
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
