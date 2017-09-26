package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.service.domain.types.CreditCard;
import nz.ac.auckland.concert.service.domain.types.Reservation;
import nz.ac.auckland.concert.service.domain.types.Seat;
import nz.ac.auckland.concert.service.domain.types.User;

import javax.persistence.EntityManager;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        _classes.add(ConcertResource.class); // resource per request
    }

    private void initialiseDataBase() {
        try {
            entityManager = PersistenceManager.instance().createEntityManager();
            entityManager.getTransaction().begin();

            removeTuplesFromEntity(Seat.class);
            removeTuplesFromEntity(CreditCard.class);
            removeTuplesFromEntity(Reservation.class);
            removeTuplesFromEntity(User.class);

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
