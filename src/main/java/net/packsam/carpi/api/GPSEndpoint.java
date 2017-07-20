package net.packsam.carpi.api;

import java.io.IOException;
import java.io.OutputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import net.packsam.carpi.model.GPSData;
import net.packsam.carpi.model.GPSMetaInfo;
import net.packsam.carpi.model.GPSPosition;
import net.packsam.carpi.model.GPSTrack;
import net.packsam.carpi.service.GPSService;
import net.packsam.carpi.service.TrackingService;

/**
 * Endpoint for the geo location.
 * 
 * @author osterrath
 *
 */
@Path("/gps")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class GPSEndpoint {
	/**
	 * GPS service.
	 */
	@Inject
	private GPSService gpsService;

	/**
	 * Tracking service.
	 */
	@Inject
	private TrackingService trackingService;

	/**
	 * Returns the last found GPS data.
	 * 
	 * @return GPS data
	 */
	@GET
	public GPSData getCurrentData() {
		GPSData ret = new GPSData();
		ret.setPosition(gpsService.getLastPosition());
		ret.setMeta(gpsService.getLastMetaInfo());
		ret.setTrack(trackingService.getCurrentPath());
		return ret;
	}

	/**
	 * Returns the last found GPS position.
	 * 
	 * @return GPS position
	 */
	@GET
	@Path("/position")
	public GPSPosition getCurrentPosition() {
		return gpsService.getLastPosition();
	}

	/**
	 * Returns the last collected GPS meta info.
	 * 
	 * @return GPS meta info
	 */
	@GET
	@Path("/meta")
	public GPSMetaInfo getCurrentMetaInfo() {
		return gpsService.getLastMetaInfo();
	}

	/**
	 * Return the current track.
	 * 
	 * @return list of path elements
	 */
	@GET
	@Path("/track")
	public GPSTrack getCurrentTrack() {
		return trackingService.getCurrentPath();
	}

	/**
	 * Returns the current track as KML file.
	 * 
	 * @return kml file
	 */
	@GET
	@Path("/track/kml")
	@Produces("application/vnd.google-earth.kml+xml")
	public Response getCurrentTrackAsKML() {
		StreamingOutput streamingOutput = new StreamingOutput() {

			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				trackingService.writeCurrentPathAsKMLToStream(output);
			}
		};
		return Response.ok(streamingOutput) //
				.header("Content-Disposition", "attachment; filename=\"track.kml\"") //
				.build();
	}

	/**
	 * Returns the current track as GPX file.
	 * 
	 * @return gpx file
	 */
	@GET
	@Path("/track/gpx")
	@Produces("application/gpx+xml")
	public Response getCurrentTrackAsGPX() {
		StreamingOutput streamingOutput = new StreamingOutput() {

			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				trackingService.writeCurrentPathAsGPXToStream(output);
			}
		};
		return Response.ok(streamingOutput) //
				.header("Content-Disposition", "attachment; filename=\"track.gpx\"") //
				.build();
	}
}
