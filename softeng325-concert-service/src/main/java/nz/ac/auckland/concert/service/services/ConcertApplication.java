package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.common.util.TheatreLayout;
import nz.ac.auckland.concert.service.domain.types.*;

import javax.persistence.EntityManager;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * JAX-RS Application subclass for the Concert Web service.
 *
 * @author Will Molloy
 */
@ApplicationPath("/services")
public class ConcertApplication extends Application {

    // This property should be used by your Resource class. It represents the
    // period of time, in seconds, that reservations are held for. If a
    // reservation isn't confirmed within this period, the reserved seats are
    // returned to the pool of seats available for booking.
    //
    // This property is used by class ConcertServiceTest.
    public static final int RESERVATION_EXPIRY_TIME_IN_SECONDS = 5;

    private EntityManager entityManager = null;


    private Set<Object> _singletons = new HashSet<>();
    private Set<Class<?>> _classes = new HashSet<>();

    public ConcertApplication() {
        initialiseDataBase(); // clear previously existing data from database

        _singletons.add(PersistenceManager.instance()); // executes db-init.sql ONCE
        _singletons.add(ConcertSubscriptionResource.instance()); // Singleton with .instance() corrupts database.. ?
        _classes.add(ConcertResource.class); // resource per request
    }

    private void initialiseDataBase() {
        try {
            entityManager = PersistenceManager.instance().createEntityManager();
            entityManager.getTransaction().begin();

            removeTuplesFromEntity(NewsItem.class);
            removeTuplesFromEntity(Seat.class);
            removeTuplesFromEntity(Reservation.class);
            removeTuplesFromEntity(CreditCard.class);
            removeTuplesFromEntity(User.class);

            // Persist all seats for all concert/dates so they can be queried and locked
            List<Concert> concerts = entityManager.createQuery("SELECT c FROM Concert c", Concert.class).getResultList();
            for (Concert concert : concerts) {
                for (LocalDateTime concertDate : concert.getDates()) {
                    for (PriceBand seatType : PriceBand.values()) {
                        for (SeatRow seatRow : TheatreLayout.getRowsForPriceBand(seatType)) {
                            IntStream.rangeClosed(1, TheatreLayout.getNumberOfSeatsForRow(seatRow)).forEach(seatNumber -> {
                                Seat seat = new Seat(seatRow, new SeatNumber(seatNumber), seatType, concertDate);
                                entityManager.persist(seat);
                            });
                        }
                    }
                }
            }

            entityManager.flush();
            entityManager.clear();

            entityManager.getTransaction().commit();
        } finally {
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    private void removeTuplesFromEntity(Class<?> myClass) {
        List<?> toRemove = entityManager.createQuery("SELECT q FROM " + myClass.getSimpleName() + " q", myClass).getResultList();
        toRemove.forEach(entityManager::remove);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return _classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return _singletons;
    }
}
