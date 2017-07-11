package net.packsam.carpi.api;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.packsam.carpi.model.HealthStatus;
import net.packsam.carpi.service.HealthService;

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
