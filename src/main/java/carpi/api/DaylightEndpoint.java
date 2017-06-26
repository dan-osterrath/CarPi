package carpi.api;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import carpi.model.DaylightData;
import carpi.service.DaylightService;

/**
 * Endpoint for getting current daylight data.
 * 
 * @author osterrath
 *
 */
@Path("/daylight")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class DaylightEndpoint {

	/**
	 * Service for calculating daylight data.
	 */
	@Inject
	private DaylightService daylightService;

	/**
	 * Returns the current daylight data.
	 * 
	 * @return dayligt data
	 */
	@GET
	public DaylightData getDaylightData() {
		return daylightService.getDaylightData();
	}
}
