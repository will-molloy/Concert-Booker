package nz.ac.auckland.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

import static nz.ac.auckland.concert.common.config.URIConfig.NEWS_ITEM_URI;

@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
@Path("/concert" + NEWS_ITEM_URI)
public class ConcertSubscriptionResource {

    private static Logger logger = LoggerFactory
            .getLogger(ConcertSubscriptionResource.class);
    private EntityManager entityManager = PersistenceManager.instance().createEntityManager();

    private List<AsyncResponse> responses = new ArrayList<>();

    private static ConcertSubscriptionResource _instance = null;

    private ConcertSubscriptionResource(){}
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

    @DELETE
    public void cancel(final @Suspended AsyncResponse response) {
        logger.info("Subscription cancelled.");
        responses.remove(response);
    }

    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public void publish(String message) {
        logger.info("Publishing: " + message);
        responses.forEach(asyncResponse -> asyncResponse.resume(message));
        responses.clear();
    }


}
