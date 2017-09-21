package nz.ac.auckland.concert.service.services;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS Resource class for the Concert Web service.
 * Defines a REST interface.
 * Supports the following HTTP messages:
 * @GET
 * 
 * @POST
 * 
 * @DELETE
 * 
 * @
 * 
 * @author Will Molloy
 */
@Path("/concerts")
@Produces({javax.ws.rs.core.MediaType.APPLICATION_XML})
@Consumes({javax.ws.rs.core.MediaType.APPLICATION_XML})
public class ConcertResource {
	
	private static Logger _logger = LoggerFactory
			.getLogger(ConcertResource.class);
	
	private EntityManager _entityManager = PersistenceManager.instance().createEntityManager();
	
	/**
	 * Begins EntityManager transaction
	 */
	private void beginTransaction(){
		_entityManager.getTransaction().begin();
	}
	
	/**
	 * Ends EntityManager transaction and close. 
	 * Call after beginTransaction().
	 */
	private void commitTransaction(){
		_entityManager.getTransaction().commit();
		_entityManager.close();
	}

}
