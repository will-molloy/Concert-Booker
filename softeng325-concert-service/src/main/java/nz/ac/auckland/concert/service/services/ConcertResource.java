package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.common.util.TheatreLayout;
import nz.ac.auckland.concert.service.domain.*;
import nz.ac.auckland.concert.service.services.mappers.ConcertMapper;
import nz.ac.auckland.concert.service.services.mappers.PerformerMapper;
import nz.ac.auckland.concert.service.services.mappers.UserMapper;
import nz.ac.auckland.concert.service.services.util.DataVerifier;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static nz.ac.auckland.concert.common.config.CookieConfig.CLIENT_COOKIE;
import static nz.ac.auckland.concert.common.config.URIConfig.*;

/**
 * JAX-RS Resource class for the Concert Web service.
 * Defines a REST interface.
 * Supports the following HTTP messages:
 *
 * @author Will Molloy
 * @GET
 * @POST
 * @DELETE
 * @
 */
@Path("/concert")
@Produces({javax.ws.rs.core.MediaType.APPLICATION_XML})
@Consumes({javax.ws.rs.core.MediaType.APPLICATION_XML})
public class ConcertResource {

    private static Logger logger = LoggerFactory
            .getLogger(ConcertResource.class);

    private EntityManager entityManager = PersistenceManager.instance().createEntityManager();

    /**
     * Begins EntityManager transaction
     */
    private void beginTransaction() {
        entityManager.getTransaction().begin();
    }

    /**
     * Ends EntityManager transaction and close.
     * Call after beginTransaction().
     */
    private void commitTransaction() {
        entityManager.getTransaction().commit();
    }

    @Path(CONCERTS_URI)
    @GET
    public Response retrieveAllConcerts(@CookieParam(CLIENT_COOKIE) Cookie clientId) {
        logger.info("Retrieving all concerts.");

        beginTransaction();
        List<Concert> concerts = entityManager.createQuery("SELECT c FROM Concert c", Concert.class).getResultList();
        commitTransaction();

        Set<ConcertDTO> concertDTOS = new HashSet<>();
        concertDTOS.addAll(concerts.stream().map(ConcertMapper::toDto).collect(Collectors.toSet()));

        GenericEntity<Set<ConcertDTO>> entity = new GenericEntity<Set<ConcertDTO>>(concertDTOS) {
        };
        return Response.ok(entity)
                .cookie(makeCookie(clientId))
                .build();
    }

    @Path(PERFORMERS_URI)
    @GET
    public Response retrieveAllPerformers(@CookieParam(CLIENT_COOKIE) Cookie clientId) {
        logger.info("Retrieving all performers.");

        beginTransaction();
        List<Performer> performers = entityManager.createQuery("SELECT p FROM Performer p", Performer.class).getResultList();
        commitTransaction();

        Set<PerformerDTO> performerDTOS = new HashSet<>();
        performerDTOS.addAll(performers.stream().map(PerformerMapper::toDto).collect(Collectors.toSet()));

        GenericEntity<Set<PerformerDTO>> entity = new GenericEntity<Set<PerformerDTO>>(performerDTOS) {
        };
        return Response.ok(entity) // 200 OK status
                .cookie(makeCookie(clientId))
                .build();
    }

    @Path(USERS_URI)
    @POST
    public Response createUser(UserDTO userDTO, @CookieParam(CLIENT_COOKIE) Cookie clientId) {
        logger.info("Attempting to persist new user: " + userDTO.getUsername());
        NewCookie newCookie = makeCookie(clientId);
        String uuid = newCookie.getValue();

        // Check all fields have been set
        if (!DataVerifier.allFieldsAreSet(userDTO)) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.CREATE_USER_WITH_MISSING_FIELDS)
                    .build());
        }
        // Check if the user already persists
        if (userExists(userDTO)) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME)
                    .build());
        }

        User user = UserMapper.toDomain(userDTO, uuid);

        // Persist the new user
        beginTransaction();
        entityManager.persist(user);
        commitTransaction();

        logger.info("Persisted new user: " + user.getUsername());

        return Response.created(URI.create(USERS_URI + "/" + userDTO.getUsername())) // 201 Created status
                .entity(userDTO)
                .cookie(newCookie)
                .build();
    }

    private boolean userExists(UserDTO userDTO) {
        beginTransaction();
        List<User> users = entityManager.createQuery("SELECT u FROM User u", User.class).getResultList();
        commitTransaction();
        return users.stream().anyMatch(user -> user.getUsername().equals(userDTO.getUsername()));
    }

    @Path(USERS_URI + LOGIN_URI)
    @POST
    public Response authenticateUser(UserDTO userDTO, @CookieParam(CLIENT_COOKIE) Cookie clientId) {
        logger.info("Attempting to authenticate user: " + userDTO.getUsername());

        // Check the username and password fields were set
        if (Objects.isNull(userDTO.getUsername()) || Objects.isNull(userDTO.getPassword())) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS)
                    .build());
        }
        // Check the user exists
        if (!userExists(userDTO)) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.AUTHENTICATE_NON_EXISTENT_USER)
                    .build());
        }

        // Retrieve the user from the database
        beginTransaction();
        User user = entityManager.find(User.class, userDTO.getUsername());
        commitTransaction();

        // Check the password was correct
        if (!user.getPassword().equals(userDTO.getPassword())) {
            throw new NotAuthorizedException(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD)
                    .build());
        }

        logger.info("Authenticated user: " + user.getUsername());

        return Response.ok(UserMapper.toDTO(user))
                .cookie(makeCookie(clientId))
                .build();
    }

    @Path(USERS_URI + RESERVATION_URI)
    @POST
    public Response makeReservation(ReservationRequestDTO reservationRequestDTO, @CookieParam(CLIENT_COOKIE) Cookie clientId) {
        logger.info("Attempting to make reservation.");

        // Check all parameters have been set in the reservation request
        if (!DataVerifier.allFieldsAreSet(reservationRequestDTO)) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS)
                    .build());
        }

        // Check request included an authentication token
        if (Objects.isNull(clientId)) {
            throw new NotAuthorizedException(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(Messages.UNAUTHENTICATED_REQUEST)
                    .build());
        }

        // Check user is authenticated i.e. exists in the database
        String uuid = clientId.getValue();
        beginTransaction();
        List<User> users = entityManager.createQuery("SELECT u FROM User u WHERE uuid = \'" + uuid + "\'", User.class).getResultList();
        commitTransaction();

        if (users.size() > 1) {
            throw new InternalServerErrorException(Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Multiple users with same UUID.")
                    .build());
        }
        if (users.isEmpty()) {
            throw new NotAuthorizedException(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(Messages.BAD_AUTHENTICATON_TOKEN)
                    .build());
        }

        // Check concert is available on that date
        Timestamp date = Timestamp.valueOf(reservationRequestDTO.getDate());
        long concertId = reservationRequestDTO.getConcertId();
        beginTransaction();
        List<Concert> concerts = entityManager.createQuery("SELECT c " +
                        "FROM Concert c JOIN c.dates d " +
                        "WHERE c.id = \'" + concertId + "\' AND " +
                        "d = \'" + date + "\'"
                , Concert.class).getResultList();
        commitTransaction();

        if (concerts.isEmpty()) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE)
                    .build());
        }

        // Check seats are available
        // Get all reservations for the requested concert, date and seat type
        PriceBand seatType = reservationRequestDTO.getSeatType();
        beginTransaction();
        List<Reservation> reservations = entityManager.createQuery("SELECT r " +
                "FROM Reservation r " +
                "WHERE r.concert = \'" + concertId + "\' " +
                "AND r.date = \'" + date + "\'" +
                "AND r.seatType = \'" + seatType + "\'"
                , Reservation.class).getResultList();
        commitTransaction();

        // Ensure the number of remaining seats is sufficient
        int numRequiredSeats = reservationRequestDTO.getNumberOfSeats();

        Set<SeatRow> rowsForSeatType = TheatreLayout.getRowsForPriceBand(seatType);
        final int numTotalSeats = rowsForSeatType.stream().mapToInt(TheatreLayout::getNumberOfSeatsForRow).sum();

        Set<Seat> reservedSeats = new HashSet<>();
        reservations.forEach(reservation -> reservedSeats.addAll(reservation.getSeats()));
        final int numReservedSeats = reservedSeats.size();
        final int numAvailableSeats = numTotalSeats - numReservedSeats;
        if (numRequiredSeats > numAvailableSeats){
            throw new WebApplicationException(Response.Status.NOT_FOUND); //TODO 404 is wrong? use 3xx ??
        }

        // Create reservation for the user
        Concert concert = concerts.get(0);
        User user = users.get(0);

        // Select seats from the set of available seats
        Set<Seat> totalSeatsInSeatType = new HashSet<>();

        for (SeatRow row : rowsForSeatType){
            for (int i = 0 ; i < TheatreLayout.getNumberOfSeatsForRow(row); i++){
                totalSeatsInSeatType.add(new Seat(row, new SeatNumber(i)));
            }
        }
        Set<Seat> availableSeats = new HashSet<>(totalSeatsInSeatType);
        availableSeats.removeAll(reservedSeats);
        while (numRequiredSeats-- > 0){

        }


        Reservation reservation = new Reservation(concert, reservationRequestDTO.getDate(), user, availableSeats);
        beginTransaction();
        entityManager.persist(reservation);
        commitTransaction();

        logger.info("Persisted new reservation: " + reservation.toString());

        return Response.created(URI.create(USERS_URI + RESERVATION_URI + "/" + reservation.toString())) // 201 Created status
                .cookie(makeCookie(clientId))
                .build();
    }

    /**
     * Helper method that can be called from every service method to generate a
     * NewCookie instance, if necessary, based on the clientId parameter.
     *
     * @param clientId the Cookie whose name is CLIENT_COOKIE, extracted
     *                 from a HTTP request message. This can be null if there was no cookie
     *                 named Config.CLIENT_COOKIE present in the HTTP request message.
     * @return a NewCookie object, with a generated UUID value, if the clientId
     * parameter is null. If the clientId parameter is non-null (i.e. the HTTP
     * request message contained a cookie named CLIENT_COOKIE), this
     * method returns null as there's no need to return a NewCookie in the HTTP
     * response message.
     */
    private NewCookie makeCookie(Cookie clientId) {
        NewCookie newCookie;
        if (clientId == null) {
            newCookie = new NewCookie(CLIENT_COOKIE, UUID.randomUUID().toString());
            logger.info("Generated cookie: " + newCookie.getValue());
        } else {
            newCookie = new NewCookie(clientId);
        }
        return newCookie;
    }

}
