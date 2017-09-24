package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.common.types.Genre;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.common.util.TheatreLayout;
import nz.ac.auckland.concert.service.services.ConcertApplication;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests for a ConcertService implementation.
 * <p>
 * Prior to running each test, an embedded servlet container is started up that
 * hosts a named Web service. Immediately after each test, the servlet
 * container is stopped. Testing is handled in this way so that the Web service
 * can reinitialise its database. Since an embedded H2 database can only have
 * one connection at a time, it's not possible for the @After method in this
 * Test class to connect to the H2 database to delete data - hence restarting
 * the Web service before each test allows the Web service to clear from the
 * database the effects of the tests.
 * <p>
 * This Test class references property RESERVATION_EXPIRY_TIME_IN_SECONDS,
 * defined in the Web service Application subclass, ConcertApplication.
 */
public class ConcertServiceTest {

    private static final int SERVER_PORT = 10000;
    private static final String WEB_SERVICE_CLASS_NAME = ConcertApplication.class.getName();
    private static Logger _logger = LoggerFactory
            .getLogger(ConcertServiceTest.class);
    private static Client _client;
    private static Server _server;

    private ConcertService _service;

    @BeforeClass
    public static void createClientAndServer() throws Exception {
        // Use ClientBuilder to create a new client that can be used to create
        // connections to the Web service.
        _client = ClientBuilder.newClient();

        // Start the embedded servlet container and host the Web service.
        ServletHolder servletHolder = new ServletHolder(new HttpServletDispatcher());
        servletHolder.setInitParameter("javax.ws.rs.Application", WEB_SERVICE_CLASS_NAME);
        ServletContextHandler servletCtxHandler = new ServletContextHandler();
        servletCtxHandler.setContextPath("/services");
        servletCtxHandler.addServlet(servletHolder, "/");
        _server = new Server(SERVER_PORT);
        _server.setHandler(servletCtxHandler);
    }

    @AfterClass
    public static void shutDown() {
        _client.close();
    }

    @Before
    public void startServer() throws Exception {
        _server.start();
        _service = new DefaultService();
    }

    @After
    public void stopServer() throws Exception {
        _server.stop();
    }

    @Test
    public void testRetrieveConcerts() {
        final int numberOfConcerts = 25;

        Set<ConcertDTO> concerts = _service.getConcerts();
        assertEquals(numberOfConcerts, concerts.size());
    }

    @Test
    public void testRetrievePerformers() {
        final int numberOfPerformers = 20;

        Set<PerformerDTO> performers = _service.getPerformers();
        assertEquals(numberOfPerformers, performers.size());
    }

    @Test
    public void testCreateUser() {
        try {
            UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
            _service.createUser(userDTO);
        } catch (ServiceException e) {
            fail();
        }
    }

    @Test
    public void testCreateUserWithMissingField() {
        try {
            UserDTO userDTO = new UserDTO(null, "123", "Churchill", "Winston");
            _service.createUser(userDTO);
            fail();
        } catch (ServiceException e) {
            assertEquals(Messages.CREATE_USER_WITH_MISSING_FIELDS, e.getMessage());
        }
    }

    @Test
    public void testCreateUserWithDuplicateUsername() {
        boolean createdFirstUser = false;
        try {
            UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
            _service.createUser(userDTO);
            createdFirstUser = true;

            userDTO = new UserDTO("Bulldog", "123", "Thatcher", "Margaret");
            _service.createUser(userDTO);
            fail();
        } catch (ServiceException e) {
            if (!createdFirstUser) {
                fail();
            }
            assertEquals(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME, e.getMessage());
        }
    }

    @Test
    public void testAuthenticateUser() {
        try {
            UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
            _service.createUser(userDTO);

            UserDTO credentials = new UserDTO("Bulldog", "123");
            UserDTO filledDTO = _service.authenticateUser(credentials);

            assertEquals(userDTO, filledDTO);
        } catch (ServiceException e) {
            fail();
        }
    }

    @Test
    public void testAuthenticateWithNonExistentUser() {
        try {
            UserDTO credentials = new UserDTO("Bulldog", "123");
            _service.authenticateUser(credentials);
            fail();
        } catch (ServiceException e) {
            assertEquals(Messages.AUTHENTICATE_NON_EXISTENT_USER, e.getMessage());
        }
    }

    @Test
    public void testAuthenticateUserWithIncorrectPassword() {
        try {
            UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
            _service.createUser(userDTO);

            UserDTO credentials = new UserDTO("Bulldog", "987");
            _service.authenticateUser(credentials);
            fail();
        } catch (ServiceException e) {
            assertEquals(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD, e.getMessage());
        }
    }

    @Test
    public void testAuthenticateUserWithMissingPassword() {
        try {
            UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
            _service.createUser(userDTO);

            UserDTO credentials = new UserDTO("Bulldog", null);
            _service.authenticateUser(credentials);
            fail();
        } catch (ServiceException e) {
            assertEquals(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS, e.getMessage());
        }
    }

    /**
     * The AWS server cannot be accessed according to the canvas announcement.
     * <p>
     * Therefore, the getImageForPerformer method should throw a SERVICE_COMMUNICATING_ERROR.
     */
    @Test
    public void testGetImageForPerformerAccessDenied() {
        try {
            Set<PerformerDTO> performers = _service.getPerformers();
            Image image = _service.getImageForPerformer(performers.iterator().next());
            fail();
        } catch (ServiceException e) {
            assertEquals(Messages.SERVICE_COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Test
    public void testGetImageForPerformerImageDoesntExist(){
        try {
            PerformerDTO unPersistedPerformer = new PerformerDTO(1L, null, null, null, new HashSet<>());
            Image image = _service.getImageForPerformer(unPersistedPerformer);
            fail();
        } catch (ServiceException e) {
            assertEquals(Messages.NO_IMAGE_FOR_PERFORMER, e.getMessage());
        }
    }

    @Test
    public void testMakeReservation() {
        try {
            final int numberOfSeatsToBook = 2;

            UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
            _service.createUser(userDTO);

            LocalDateTime dateTime = LocalDateTime.of(2017, 2, 24, 17, 00);
            ReservationRequestDTO request = new ReservationRequestDTO(numberOfSeatsToBook, PriceBand.PriceBandC, 1L, dateTime);

            ReservationDTO reservation = _service.reserveSeats(request);

            ReservationRequestDTO requestFromResponse = reservation.getReservationRequest();
            assertEquals(request, requestFromResponse);

            Set<SeatDTO> reservedSeats = reservation.getSeats();
            assertEquals(numberOfSeatsToBook, reservedSeats.size());

            // Check that the seats reserved are of the required type.
            for (SeatDTO seat : reservedSeats) {
                assertTrue(TheatreLayout.getRowsForPriceBand(PriceBand.PriceBandC).contains(seat.getRow()));
            }

        } catch (ServiceException e) {
            fail();
        }
    }

    @Test
    public void testMakeReservationMissingFields() {
        try {
            UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
            _service.createUser(userDTO);

            LocalDateTime dateTime = LocalDateTime.of(2018, 2, 24, 17, 00);
            ReservationRequestDTO request = new ReservationRequestDTO(2, null, 1L, dateTime);

            ReservationDTO reservation = _service.reserveSeats(request);
            fail();
        } catch (ServiceException e) {
            assertEquals(Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS, e.getMessage());
        }
    }

    @Test
    public void testMakeReservationWithBadRequest() {
        try {
            UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
            _service.createUser(userDTO);

            LocalDateTime dateTime = LocalDateTime.of(2018, 2, 24, 17, 00);
            ReservationRequestDTO request = new ReservationRequestDTO(2, PriceBand.PriceBandC, 1L, dateTime);

            ReservationDTO reservation = _service.reserveSeats(request);
            fail();
        } catch (ServiceException e) {
            assertEquals(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE, e.getMessage());
        }
    }

    @Test
    public void testMakeReservationWhereSeatsAreNotAvailable() {
        try {
            UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
            _service.createUser(userDTO);

            Set<SeatRow> rowsOfStandardSeats = TheatreLayout.getRowsForPriceBand(PriceBand.PriceBandB);
            int totalNumberOfStandardSeats = 0;
            for (SeatRow row : rowsOfStandardSeats) {
                totalNumberOfStandardSeats += TheatreLayout.getNumberOfSeatsForRow(row);
            }

            LocalDateTime dateTime = LocalDateTime.of(2017, 2, 24, 17, 00);
            ReservationRequestDTO request = new ReservationRequestDTO(totalNumberOfStandardSeats, PriceBand.PriceBandB, 1L, dateTime);

            ReservationDTO reservation = _service.reserveSeats(request);

            ReservationRequestDTO requestFromResponse = reservation.getReservationRequest();
            assertEquals(request, requestFromResponse);

            Set<SeatDTO> reservedSeats = reservation.getSeats();
            assertEquals(totalNumberOfStandardSeats, reservedSeats.size());

            // Check that the seats reserved are of the required type.
            for (SeatDTO seat : reservedSeats) {
                assertTrue(TheatreLayout.getRowsForPriceBand(PriceBand.PriceBandB).contains(seat.getRow()));
            }

            // Attempt to reserve another seat.
            request = new ReservationRequestDTO(1, PriceBand.PriceBandB, 1L, dateTime);
            reservation = _service.reserveSeats(request);
            fail();
        } catch (ServiceException e) {
            assertEquals(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION, e.getMessage());
        }
    }

    @Test
    public void testMakeReservationWithUnauthenticatedUser() {
        try {
            final int numberOfSeatsToBook = 10;

            LocalDateTime dateTime = LocalDateTime.of(2017, 2, 24, 17, 00);
            ReservationRequestDTO request = new ReservationRequestDTO(numberOfSeatsToBook, PriceBand.PriceBandA, 1L, dateTime);

            ReservationDTO reservation = _service.reserveSeats(request);
            fail();
        } catch (ServiceException e) {
            assertEquals(Messages.UNAUTHENTICATED_REQUEST, e.getMessage());
        }
    }

    @Test
    public void testConfirmReservation() {
        try {
            final int numberOfSeatsToBook = 5;

            // Create a User and register a credit card.
            UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
            _service.createUser(userDTO);
            CreditCardDTO creditCard = new CreditCardDTO(CreditCardDTO.Type.Visa, "Winston Churchill", "4929-1500-0055-9544", LocalDate.of(2019, 7, 31));
            _service.registerCreditCard(creditCard);

            // Make a reservation.
            LocalDateTime dateTime = LocalDateTime.of(2017, 2, 24, 17, 00);
            ReservationRequestDTO request = new ReservationRequestDTO(numberOfSeatsToBook, PriceBand.PriceBandC, 1L, dateTime);
            ReservationDTO reservation = _service.reserveSeats(request);

            // Confirm the reservation.
            _service.confirmReservation(reservation);

            // Make a request to check that this user has a booking.
            Set<BookingDTO> bookings = _service.getBookings();
            assertEquals(1, bookings.size());
            BookingDTO bookingDTO = bookings.iterator().next();
            assertEquals(new Long(1), bookingDTO.getConcertId());
            assertEquals(dateTime, bookingDTO.getDateTime());
            assertEquals(reservation.getSeats(), bookingDTO.getSeats());
            assertEquals(PriceBand.PriceBandC, bookingDTO.getPriceBand());
        } catch (ServiceException e) {
            fail();
        }
    }

    @Test
    public void testConfirmBookingWithExpiredReservation() {
        try {
            final int numberOfSeatsToBook = 12;

            // Create a User and register a credit card.
            UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
            _service.createUser(userDTO);
            CreditCardDTO creditCard = new CreditCardDTO(CreditCardDTO.Type.Visa, "Winston Churchill", "4929-1500-0055-9544", LocalDate.of(2019, 7, 31));
            _service.registerCreditCard(creditCard);

            // Make a reservation.
            LocalDateTime dateTime = LocalDateTime.of(2017, 2, 24, 17, 00);
            ReservationRequestDTO request = new ReservationRequestDTO(numberOfSeatsToBook, PriceBand.PriceBandA, 1L, dateTime);
            ReservationDTO reservation = _service.reserveSeats(request);

            // Wait for the reservation to expire.
            Thread.sleep(ConcertApplication.RESERVATION_EXPIRY_TIME_IN_SECONDS * 1000);

            // Attempt to confirm the reservation.
            _service.confirmReservation(reservation);
            fail();
        } catch (ServiceException e) {
            assertEquals(Messages.EXPIRED_RESERVATION, e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Make a request to check that this user doesn't have a booking.
            Set<BookingDTO> bookings = _service.getBookings();
            assertTrue(bookings.isEmpty());
        }
    }

    @Test
    public void testConfirmReservationWithoutRegisteredCreditCard() {
        try {
            final int numberOfSeatsToBook = 6;

            // Create a User.
            UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
            _service.createUser(userDTO);

            // Make a reservation.
            LocalDateTime dateTime = LocalDateTime.of(2017, 2, 24, 17, 00);
            ReservationRequestDTO request = new ReservationRequestDTO(numberOfSeatsToBook, PriceBand.PriceBandB, 1L, dateTime);
            ReservationDTO reservation = _service.reserveSeats(request);

            // Attempt to confirm the reservation.
            _service.confirmReservation(reservation);
            fail();
        } catch (ServiceException e) {
            assertEquals(Messages.CREDIT_CARD_NOT_REGISTERED, e.getMessage());
        } finally {
            // Make a request to check that this user doesn't have a booking.
            Set<BookingDTO> bookings = _service.getBookings();
            assertTrue(bookings.isEmpty());
        }
    }

    @Test
    public void testRegisterCreditCard() {
        try {
            UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
            _service.createUser(userDTO);

            CreditCardDTO creditCard = new CreditCardDTO(CreditCardDTO.Type.Visa, "Winston Churchill", "4929-1500-0055-9544", LocalDate.of(2019, 7, 31));
            _service.registerCreditCard(creditCard);
        } catch (ServiceException e) {
            fail();
        }
    }

    @Test
    public void testRegisterCreditCardWithUnauthenticatedUser() {
        try {
            CreditCardDTO creditCard = new CreditCardDTO(CreditCardDTO.Type.Visa, "Winston Churchill", "4929-1500-0055-9544", LocalDate.of(2019, 7, 31));
            _service.registerCreditCard(creditCard);
            fail();
        } catch (ServiceException e) {
            assertEquals(Messages.UNAUTHENTICATED_REQUEST, e.getMessage());
        }
    }
}
