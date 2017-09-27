package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;
import nz.ac.auckland.concert.service.domain.types.NewsItem;
import nz.ac.auckland.concert.service.services.mappers.NewsItemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static nz.ac.auckland.concert.common.config.CookieConfig.CLIENT_COOKIE;
import static nz.ac.auckland.concert.common.config.URIConfig.NEWS_ITEM_URI;
import static nz.ac.auckland.concert.service.services.ConcertResource.makeCookie;

@Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
@Produces({MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
@Path("/concert" + NEWS_ITEM_URI)
public class ConcertSubscriptionResource {

    private Map<String, LocalDateTime> pausedClients = new HashMap<>();
    private Map<String, AsyncResponse> responses = new HashMap<>();
    private List<String> registeredClients = new ArrayList<>();

    /**
     * Begins EntityManager transaction
     */
    private void beginTransaction() {
        entityManager.getTransaction().begin();
    }

    /**
     * Ends EntityManager transaction and closes. ConcertApplication class will re initialise the entity manager.
     * Call after beginTransaction().
     */
    private void commitTransaction() {
        entityManager.getTransaction().commit();
    }

    private static Logger logger = LoggerFactory
            .getLogger(ConcertSubscriptionResource.class);
    private EntityManager entityManager = PersistenceManager.instance().createEntityManager();

    private static ConcertSubscriptionResource _instance = null;

    private Executor executor = new ThreadPoolExecutor(5, 5, 0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(10));

    private ConcertSubscriptionResource() {
    }

    public static ConcertSubscriptionResource instance() {
        if (_instance == null) {
            _instance = new ConcertSubscriptionResource();
        }
        return _instance;
    }

    @Path("/register")
    @GET
    public Response register(@CookieParam(CLIENT_COOKIE) Cookie clientId) {
        logger.info("Sending registration code");
        NewCookie newCookie = makeCookie(clientId);
        registeredClients.add(newCookie.getValue());

        return Response.ok().cookie(newCookie).build();
    }

    /**
     * Links the given async response to the given client.
     * If this is a returning client broadcasts all messages they missed.
     */
    @GET
    public void subscribe(final @Suspended AsyncResponse newResponse, @CookieParam(CLIENT_COOKIE) Cookie clientId) {
        String registrationId = null;
        if (clientId != null) {
            registrationId = clientId.getValue();
        }
        String finalRegistrationId = registrationId;
        executor.execute(() -> {
            responses.put(finalRegistrationId, newResponse);

            if (responses.containsKey(finalRegistrationId)) {
                logger.info("Returning subscriber." + finalRegistrationId);

                if (pausedClients.containsKey(finalRegistrationId)) {
                    // Notify response with news items it may have missed
                    LocalDateTime resubscribeTime = LocalDateTime.now();
                    LocalDateTime cancelTime = pausedClients.get(finalRegistrationId);
                    pausedClients.remove(finalRegistrationId);

                    beginTransaction();
                    List<NewsItem> oldNewsItems = entityManager.createQuery("SELECT n FROM NewsItem n", NewsItem.class).getResultList();
                    commitTransaction();
                    Set<NewsItemDTO> missedItems = new HashSet<>();
                    for (NewsItem oldItem : oldNewsItems) {
                        LocalDateTime itemTime = oldItem.get_timestamp();
                        if (itemTime.isBefore(resubscribeTime) && itemTime.isAfter(cancelTime)) {
                            missedItems.add(NewsItemMapper.toDTO(oldItem));
                        }
                    }
                    GenericEntity<Set<NewsItemDTO>> entity = new GenericEntity<Set<NewsItemDTO>>(missedItems) {
                    };
                    Response response = Response.ok().entity(entity).build();
                    newResponse.resume(response);
                }
            } else {
                logger.info("New subscriber.");
            }
        });
    }

    /**
     * Broadcasts the message to all subscribed clients that aren't paused.
     */
    @POST
    public void publish(final String message) {
        logger.info("Publishing: " + message);
        executor.execute(() -> {
            // Store the news item
            beginTransaction();
            final NewsItem newsItem = new NewsItem(LocalDateTime.now(), message);
            entityManager.persist(newsItem);
            commitTransaction();

            // Notify registered clients that aren't paused
            for (String registeredClient : registeredClients) {
                AsyncResponse asyncResponse = responses.get(registeredClient);
                if (!pausedClients.containsKey(registeredClient) && asyncResponse != null) {
                    Set<NewsItemDTO> newsItemSet = new HashSet<>();
                    newsItemSet.add(NewsItemMapper.toDTO(newsItem));
                    GenericEntity<Set<NewsItemDTO>> entity = new GenericEntity<Set<NewsItemDTO>>(newsItemSet) {
                    };
                    Response response = Response.ok().entity(entity).build();
                    asyncResponse.resume(response);
                }

            }
        });
    }

    /**
     * Pauses the given client
     */
    @DELETE
    public void cancel(@CookieParam(CLIENT_COOKIE) Cookie clientId) {
        pausedClients.put(clientId.getValue(), LocalDateTime.now());
    }
}
