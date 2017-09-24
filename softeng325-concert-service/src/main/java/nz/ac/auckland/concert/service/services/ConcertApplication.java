package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.service.domain.Reservation;
import nz.ac.auckland.concert.service.domain.User;

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

    private Set<Object> _singletons = new HashSet<>();
    private Set<Class<?>> _classes = new HashSet<>();

    public ConcertApplication() {
        initialiseDataBase(); // clear previously existing data from database

        _singletons.add(PersistenceManager.instance()); // executes db-init.sql ONCE
        _classes.add(ConcertResource.class); // resource per request
    }

    private void initialiseDataBase() {
        EntityManager entityManager = null;
        try {
            entityManager = PersistenceManager.instance().createEntityManager();
            entityManager.getTransaction().begin();

            List<Reservation> reservations = entityManager.createQuery("SELECT r FROM Reservation r", Reservation.class).getResultList();
            reservations.forEach(entityManager::remove);

            List<User> users = entityManager.createQuery("SELECT u FROM User u", User.class).getResultList();
            users.forEach(entityManager::remove);

            entityManager.flush();
            entityManager.clear();

            entityManager.getTransaction().commit();
        } finally {
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
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
