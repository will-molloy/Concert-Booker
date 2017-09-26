package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.service.domain.types.*;
import nz.ac.auckland.concert.service.services.mappers.*;
import nz.ac.auckland.concert.service.services.util.DataVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static nz.ac.auckland.concert.common.config.CookieConfig.CLIENT_COOKIE;
import static nz.ac.auckland.concert.common.config.URIConfig.*;
import static nz.ac.auckland.concert.common.message.Messages.RESOURCE_MODIFICATION_ERROR;
import static nz.ac.auckland.concert.service.services.ConcertApplication.RESERVATION_EXPIRY_TIME_IN_SECONDS;

/**
 * JAX-RS Resource class for the Concert Web service.
 * Defines a REST interface.
 * Supports the following HTTP messages:
 *
 *
 * //TODO documentation
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
        entityManager.close();
    }

    @Path(CONCERTS_URI)
    @GET
    public Response retrieveAllConcerts(@CookieParam(CLIENT_COOKIE) Cookie clientId) {
        logger.info("Retrieving all concerts.");

        beginTransaction();

        List<Concert> concerts = entityManager.createQuery("SELECT c FROM Concert c", Concert.class).getResultList();

        Set<ConcertDTO> concertDTOS = new HashSet<>();
        concertDTOS.addAll(concerts.stream().map(ConcertMapper::toDTO).collect(Collectors.toSet()));

        commitTransaction();

        GenericEntity<Set<ConcertDTO>> entity = new GenericEntity<Set<ConcertDTO>>(concertDTOS) {
        };
        return Response.ok(entity) // 200 OK status
                .cookie(makeCookie(clientId))
                .build();
    }

    @Path(PERFORMERS_URI)
    @GET
    public Response retrieveAllPerformers(@CookieParam(CLIENT_COOKIE) Cookie clientId) {
        logger.info("Retrieving all performers.");

        beginTransaction();

        List<Performer> performers = entityManager.createQuery("SELECT p FROM Performer p", Performer.class).getResultList();

        Set<PerformerDTO> performerDTOS = new HashSet<>();
        performerDTOS.addAll(performers.stream().map(PerformerMapper::toDTO).collect(Collectors.toSet()));

        commitTransaction();

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

        // Check all fields have been set for the user
        if (!DataVerifier.allFieldsAreSet(userDTO)) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.CREATE_USER_WITH_MISSING_FIELDS)
                    .build());
        }

        beginTransaction();

        // Check if the user already persists
        if (userExists(userDTO)) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME)
                    .build());
        }

        // Persist the new user
        User user = UserMapper.toDomain(userDTO, uuid);
        entityManager.persist(user);

        commitTransaction();

        logger.info("Persisted new user: " + user.getUsername());

        return Response.created(URI.create(USERS_URI + "/" + userDTO.getUsername())) // 201 Created status
                .entity(userDTO)
                .cookie(newCookie)
                .build();
    }

    private boolean userExists(UserDTO userDTO) {
        List<User> users = entityManager.createQuery("SELECT u FROM User u", User.class).getResultList();
        return users.stream().anyMatch(user -> user.getUsername().equals(userDTO.getUsername()));
    }

    @Path(USERS_URI + AUTHENTICATE_URI)
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
        beginTransaction();
        if (!userExists(userDTO)) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.AUTHENTICATE_NON_EXISTENT_USER)
                    .build());
        }

        // Retrieve the user from the database
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
        List<Seat> requestedSeats;
        Reservation newReservation = null;
        try {
            beginTransaction();

            // Check request included an authentication token that's valid and retrieve the logged in user
            User user = checkAuthenticationTokenAndGetUser(clientId);

            Timestamp date = Timestamp.valueOf(reservationRequestDTO.getDate());
            long concertId = reservationRequestDTO.getConcertId();
            List<Concert> concerts = entityManager.createQuery("SELECT c " +
                            "FROM Concert c JOIN c.dates d " +
                            "WHERE c.id = \'" + concertId + "\' AND " +
                            "d = \'" + date + "\'"
                    , Concert.class)
                    .getResultList();

            if (concerts.isEmpty()) {
                throw new BadRequestException(Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE)
                        .build());
            }
            Concert concert = concerts.get(0);
            PriceBand seatType = reservationRequestDTO.getSeatType();
            LocalDateTime dateTime = reservationRequestDTO.getDate();
            int numRequiredSeats = reservationRequestDTO.getNumberOfSeats();

            // Select the required number of seats for the given concert date and price band, that aren't reserved
            // This set of seats will be locked since they will be updated by a link to the reservation
            removeExpiredReservations();
            requestedSeats = entityManager.createQuery(
                    "SELECT s " +
                            "FROM Seat s " +
                            "WHERE s.concertDate = :date " +
                            "AND s.seatType = :seatType " +
                            "AND s.reservation IS NULL"
                    , Seat.class)
                    .setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
                    .setMaxResults(numRequiredSeats)
                    .setParameter("date", dateTime)
                    .setParameter("seatType", seatType)
                    .getResultList();
            if (requestedSeats.size() != numRequiredSeats) {
                throwInsufficientSeatsException();
            }

            // Persist reservation for the user
            long reservationExpiryTime = System.currentTimeMillis() + (RESERVATION_EXPIRY_TIME_IN_SECONDS * 1000);
            newReservation = new Reservation(concert, dateTime, seatType, new HashSet<>(requestedSeats), user, reservationExpiryTime);
            entityManager.persist(newReservation); // links requestedSeats to reservation
            logger.info("Persisted new reservation: " + newReservation.toString());

        } catch (NoResultException e) {
            throwInsufficientSeatsException();
        } catch (OptimisticLockException e){
            throw new WebApplicationException(Response.
                    status(Response.Status.PRECONDITION_FAILED)
                    .entity(RESOURCE_MODIFICATION_ERROR)
                    .build());
        } finally {
            commitTransaction();
        }

        return Response.created(URI.create(USERS_URI + RESERVATION_URI + "/" + newReservation.toString())) // 201 Created status
                .entity(ReservationMapper.toReservationDTO(newReservation))
                .cookie(makeCookie(clientId))
                .build();
    }

    private void removeExpiredReservations() {
        List<Reservation> expiredReservations = entityManager.createQuery("SELECT r FROM Reservation r " +
                "WHERE r.expiryTime < " + System.currentTimeMillis(), Reservation.class).getResultList();
        int size = expiredReservations.size();
        expiredReservations.forEach(entityManager::remove);
        logger.info("Removed: " + size + " expired reservation(s).");
    }

    private void throwInsufficientSeatsException() throws BadRequestException{
        throw new BadRequestException(Response
                .status(Response.Status.BAD_REQUEST)
                .entity(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION)
                .build());
    }

    /**
     * Ensures that an authentication token is valid: i.e. it was provided and maps to a single user
     * in the database. Then returns that user.
     *
     * @throws NotAuthorizedException       if the cookie doesn't contain a UUID.
     * @throws NotAuthorizedException       if that user isn't found in the database.
     * @throws InternalServerErrorException if multiple users have the same authentication token.
     */
    private User checkAuthenticationTokenAndGetUser(Cookie cookie) throws InternalServerErrorException, NotAuthorizedException {
        if (Objects.isNull(cookie)) {
            throw new NotAuthorizedException(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(Messages.UNAUTHENTICATED_REQUEST)
                    .build());
        }

        String uuid = cookie.getValue();
        List<User> users = entityManager.createQuery("SELECT u FROM User u WHERE uuid = \'" + uuid + "\'", User.class).getResultList();

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
        return users.get(0);
    }

    @Path(USERS_URI + PAYMENT_URI)
    @POST
    public Response registerCreditCard(CreditCardDTO creditCardDTO, @CookieParam(CLIENT_COOKIE) Cookie clientId) {
        beginTransaction();

        User user = checkAuthenticationTokenAndGetUser(clientId);
        logger.info("Registering credit card for user :" + user.getUsername());
        CreditCard creditCard = CreditCardMapper.toDomain(creditCardDTO, user);
        entityManager.persist(creditCard);

        commitTransaction();

        return Response.noContent() // 204 No Content status
                .cookie(makeCookie(clientId))
                .build();
    }

    @Path(USERS_URI + RESERVATION_URI + CONFIRM_URI)
    @POST
    public Response confirmReservation(ReservationDTO reservationDTO, @CookieParam(CLIENT_COOKIE) Cookie clientId) {
        beginTransaction();

        // Check the reservation is still valid by removing expired reservations
        removeExpiredReservations();

        User user = checkAuthenticationTokenAndGetUser(clientId);
        logger.info("Confirming reservation for user :" + user.getUsername());
        try {
            // Lock the reservation before confirming
            Reservation reservation = entityManager.createQuery(
                    "SELECT r FROM Reservation r " +
                            "WHERE r.user = \'" + user.getUsername() + "\' " +
                            "AND r.id = " + reservationDTO.getId(),
                    Reservation.class).setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT).getSingleResult();

            // Check user has a registered credit card
            CreditCard creditCard = user.getCreditCard();
            if (Objects.isNull(creditCard)) {
                throw new BadRequestException(Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(Messages.CREDIT_CARD_NOT_REGISTERED)
                        .build());
            }

            // Confirm the reservation
            reservation.setConfirmed(true);
            reservation.setExpiryDate(Long.MAX_VALUE);
            entityManager.merge(reservation);

        } catch (NoResultException e) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.EXPIRED_RESERVATION)
                    .build());
        } finally {
            commitTransaction();
        }

        return Response.noContent() // 204 No Content status
                .cookie(makeCookie(clientId))
                .build();
    }

    @Path(USERS_URI + RESERVATION_URI)
    @GET
    public Response getBookings(@CookieParam(CLIENT_COOKIE) Cookie clientId) {
        beginTransaction();

        User user = checkAuthenticationTokenAndGetUser(clientId);
        removeExpiredReservations();

        logger.info("Retrieving bookings for user :" + user.getUsername());

        List<Reservation> reservations = entityManager.createQuery("SELECT r " +
                "FROM Reservation r " +
                "WHERE r.user = \'" + user.getUsername() + "\' " +
                "AND r.confirmed = " + true, Reservation.class).getResultList();

        Set<BookingDTO> bookingsDTOs = new HashSet<>();
        bookingsDTOs.addAll(reservations.stream().map(ReservationMapper::toBookingDTO).collect(Collectors.toSet()));

        commitTransaction();

        GenericEntity<Set<BookingDTO>> entity = new GenericEntity<Set<BookingDTO>>(bookingsDTOs) {
        };
        return Response.ok(entity)
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
