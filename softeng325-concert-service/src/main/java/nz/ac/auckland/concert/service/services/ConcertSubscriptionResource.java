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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static nz.ac.auckland.concert.common.config.URIConfig.NEWS_ITEM_URI;

@Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
@Produces({MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
@Path("/concert" + NEWS_ITEM_URI)
public class ConcertSubscriptionResource {

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

    private List<AsyncResponse> responses = new ArrayList<>();

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

    @GET
    public void subscribe(final @Suspended AsyncResponse response) {
        logger.info("New subscriber.");
        responses.add(response);
    }

    @POST
    public void publish(final String message) {
        logger.info("Publishing: " + message);
        executor.execute(() -> {
            beginTransaction();
            final NewsItem newsItem = new NewsItem(LocalDateTime.now(), message);
            entityManager.persist(newsItem);
            commitTransaction();

            NewsItemDTO newsItemDTO = NewsItemMapper.toDTO(newsItem);
            responses.forEach(asyncResponse -> asyncResponse.resume(newsItemDTO));
            responses.clear();
        });
    }


}
