package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.message.Messages;
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
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertNotNull;
import static nz.ac.auckland.concert.common.config.URIConfig.NEWS_ITEM_URI;
import static nz.ac.auckland.concert.common.config.URIConfig.WEB_SERVICE_URI;
import static org.junit.Assert.*;

/**
 * Additional tests on getImageForPerformer and subscription model.
 * Ignored since they require toggling of debug code in ConcertApplication.
 */
public class AdditionalTests {
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
    @Ignore
    public void validImageForPerformer() {
        Set<PerformerDTO> performers = _service.getPerformers();
        PerformerDTO p1 = performers.iterator().next();
        Image img = _service.getImageForPerformer(p1);
        assertNotNull(img);
    }

    @Test
    @Ignore
    public void invalidImageForPerformer() {
        Set<PerformerDTO> performers = _service.getPerformers();
        _logger.debug(performers.toString());
        PerformerDTO p1 = performers.stream().filter(p -> p.getImageName() != null && p.getImageName().equals("fake.jpg")).collect(Collectors.toList()).get(0);
        Image img = _service.getImageForPerformer(p1);
        assertNull(img);
    }

    @Test
    @Ignore
    public void noImageForPerformer() {
        Set<PerformerDTO> performers = _service.getPerformers();
        for (PerformerDTO p : performers) {
            _logger.debug(p.getImageName());
        }
        PerformerDTO p1 = performers.stream().filter(p -> p.getImageName() == null).collect(Collectors.toList()).get(0);
        try {
            Image img = _service.getImageForPerformer(p1);
            fail();
        } catch (ServiceException e) {
            assertEquals(e.getMessage(), Messages.NO_IMAGE_FOR_PERFORMER);
        }
    }

    @Test
    public void testPublish() throws InterruptedException {
        MockItemListener listener = new MockItemListener() {
            @Override
            public void newsItemReceived(NewsItemDTO newsItem) {
                assertEquals(newsItem.getContent(),"hi");
                incrementExecution();
            }
        };
        _service.subscribeForNewsItems(listener);
        Thread.sleep(2000);
        Invocation.Builder b = _client.target(WEB_SERVICE_URI+NEWS_ITEM_URI).request();
        Response r = b.post(Entity.entity("hi",MediaType.TEXT_PLAIN));
        Thread.sleep(2000);
        Invocation.Builder b2 = _client.target(WEB_SERVICE_URI+NEWS_ITEM_URI).request();
        Response r2 = b2.post(Entity.entity("hi",MediaType.TEXT_PLAIN));
        Thread.sleep(2000);
        r.close();
        r2.close();
        _service.cancelSubscription();
        assertEquals(2,listener.getExecutedTimes());
    }

    @Test
    public void testPublishWithIntervalBetween() throws InterruptedException {
        MockItemListener listener = new MockItemListener() {
            @Override
            public void newsItemReceived(NewsItemDTO newsItem) {
                assertEquals(newsItem.getContent(),"hi");
                incrementExecution();
            }
        };
        //Subscribe and get 1 response.
        _service.subscribeForNewsItems(listener);
        _service.cancelSubscription();
        Thread.sleep(200);
        Invocation.Builder b = _client.target(WEB_SERVICE_URI+NEWS_ITEM_URI).request();
        Response r = b.post(Entity.entity("hi",MediaType.TEXT_PLAIN));
        Thread.sleep(200);
        //Blackout period
        Invocation.Builder b2 = _client.target(WEB_SERVICE_URI+NEWS_ITEM_URI).request();
        Response r2 = b2.post(Entity.entity("hi",MediaType.TEXT_PLAIN));
        //Resubscribe and get back messages missed since last subscribe
        _service.subscribeForNewsItems(listener);
        _service.cancelSubscription();
        Thread.sleep(200);
        Invocation.Builder b3 = _client.target(WEB_SERVICE_URI+NEWS_ITEM_URI).request();
        Response r3 = b2.post(Entity.entity("hi",MediaType.TEXT_PLAIN));
        r.close();
        r2.close();
        r3.close();
        Thread.sleep(300);
        assertEquals(3,listener.getExecutedTimes());
    }

    abstract class MockItemListener implements ConcertService.NewsItemListener {
        private AtomicInteger executedTimes = new AtomicInteger(0);

        public int getExecutedTimes() {
            return executedTimes.get();
        }

        public int incrementExecution() {
            return executedTimes.incrementAndGet();
        }
    }
}
