package nz.ac.auckland.concert.client.service;

import java.awt.Image;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;

public class DefaultService implements ConcertService {

	@Override
	public Set<ConcertDTO> getConcerts() throws ServiceException {
		
		// PIAZZA CODE:: PLACE IN EVERY METHOD ( create private one )
		
		// Create a new Client (connection).
		Client client = ClientBuilder.newClient();
		// Prepare a request and invoke the Web service.
		Builder builder = null;
		Response response = builder.get();
		// Process the response.
		// Close both the Response and the connection.
		response.close();
		client.close();
		
		
		
		return null;
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
