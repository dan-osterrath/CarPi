package carpi.api;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import carpi.model.StreamedResource;
import carpi.service.MapService;

/**
 * JAX-RS endpoint for rendering map data.
 * 
 * @author osterrath
 *
 */
@Path("/map")
@ApplicationScoped
public class MapEndpoint extends StreamingEndpoint {
	/**
	 * Service for reading map tiles.
	 */
	@Inject
	private MapService mapService;

	/**
	 * Returns the map tile for the given coordinates and zoom level.
	 * 
	 * @param z
	 *            zoom level
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param request
	 * @return response
	 */
	@GET
	@Path("{z}/{x}/{y}")
	public Response getTile(@PathParam("z") int z, @PathParam("x") int x, @PathParam("y") int y, @Context Request request) {
		// try to get file
		StreamedResource resource = mapService.getTile(z, x, y);

		if (resource != null) {
			// send resource to client
			return createResponse(resource, request, false);
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}
}
