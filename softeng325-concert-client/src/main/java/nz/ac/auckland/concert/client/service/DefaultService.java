package nz.ac.auckland.concert.client.service;

import java.awt.Image;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;

public class DefaultService implements ConcertService {

    private Client client;
    private Response response;
    private static final String WEB_SERVICE_URI = "http://localhost:10000/services";
    private static final String CONCERTS_URI = "/concerts";

    /**
     * Creates a new client connection
     */
    private void createClientConnection(){
        client = ClientBuilder.newClient();
    }

    /**
     * Closes the response and client connections
     */
    private void closeReponseAndClient(){
        response.close();
        client.close();
    }

    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {
        createClientConnection();

        Builder builder = client.target(WEB_SERVICE_URI + CONCERTS_URI).request().accept(MediaType.APPLICATION_XML);
        response = builder.get();

        Set<ConcertDTO> concerts = response
                .readEntity(new GenericType<Set<ConcertDTO>>(){
                });

        closeReponseAndClient();
        return concerts;
    }

    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        // TODO Auto-generated method stub
        return null;
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
