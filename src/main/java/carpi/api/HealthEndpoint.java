/**
 * (C) 2017 by 3m5. Media GmbH. http://www.3m5.de
 */
package carpi.api;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import carpi.model.HealthStatus;
import carpi.service.HealthService;

/**
 * JAX-RS endpoint for getting host health status.
 * 
 * @author osterrath
 *
 */
@Path("/health")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class HealthEndpoint {

	/**
	 * Service for getting health status.
	 */
	@Inject
	private HealthService healthService;

	/**
	 * Returns the current health status.
	 * 
	 * @return health status
	 */
	@GET
	public HealthStatus getHealthStatus() {
		return healthService.getHealthStatus();
	}
}
