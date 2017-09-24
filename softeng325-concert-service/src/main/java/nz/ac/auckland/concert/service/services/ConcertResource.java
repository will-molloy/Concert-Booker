package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Performer;
import nz.ac.auckland.concert.service.domain.User;
import nz.ac.auckland.concert.service.services.mappers.ConcertMapper;
import nz.ac.auckland.concert.service.services.mappers.PerformerMapper;
import nz.ac.auckland.concert.service.services.mappers.UserMapper;
import nz.ac.auckland.concert.service.services.util.DataVerifier;
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
            throw new WebApplicationException(Response.Status.BAD_REQUEST); // 400 Bad Request status
        }
        // Check if the user already persists
        else if (userExists(userDTO)) {
            throw new WebApplicationException(Response.Status.CONFLICT); // 409 Conflict status
        }

        User user = UserMapper.toDomain(userDTO, uuid);

        // Persist the new user
        beginTransaction();
        entityManager.persist(user);
        commitTransaction();

        logger.info("Persisted new user: " + user.getUsername());

        return Response.created(URI.create(USERS_URI + "/" + userDTO.getUsername())) // 201 Created status
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
            throw new WebApplicationException(Response.Status.BAD_REQUEST); // 400
        }
        // Check the user exists
        else if (!userExists(userDTO)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); // 404
        }

        // Retrieve the user from the database
        beginTransaction();
        User user = entityManager.find(User.class, userDTO.getUsername());
        commitTransaction();

        // Check the password was correct
        if (!user.getPassword().equals(userDTO.getPassword())) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED); // 401
        }

        logger.info("Authenticated user: " + user.getUsername());

        return Response.ok(UserMapper.toDTO(user))
                .cookie(makeCookie(clientId))
                .build();
    }

    // TODO decide on URI? Post - return a newly created one.. PUT - client decides. Must change in ClientService too.
    // TODO https://stackoverflow.com/questions/6203231/which-http-methods-match-up-to-which-crud-methods
    @Path(USERS_URI + RESERVATION_URI)
    @POST
    public Response makeReservation(ReservationRequestDTO reservationRequestDTO, @CookieParam(CLIENT_COOKIE) Cookie clientId) {
        logger.info("Attempting to make reservation.");

        // Check all parameters have been set in the reservation request
        if (!DataVerifier.allFieldsAreSet(reservationRequestDTO)) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        // Check request included an authentication token
        if (Objects.isNull(clientId)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        // Check user is authenticated i.e. exists in the database
        String uuid = clientId.getValue();
        beginTransaction();
        List<User> users = entityManager.createQuery("SELECT u FROM User u WHERE uuid = \'" + uuid + "\'", User.class).getResultList();
        commitTransaction();

        if (users.size() > 1) {
            // Multiple users with same UUID
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
        if (users.isEmpty()) {
            // User included UUID but wasn't found in the database
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
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
            // The requested reservation contained a date/time for when the concert is not scheduled.
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        // Check the seats are available


        // Create the reservation for that user


        return null;
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
