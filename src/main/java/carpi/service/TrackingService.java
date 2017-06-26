package carpi.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticMeasurement;
import org.gavaghan.geodesy.GlobalPosition;

import carpi.config.CarpiConfiguration;
import carpi.event.GPSPositionChangeEvent;
import carpi.model.GPSPathElement;
import carpi.model.GPSPosition;
import carpi.model.GPSTrack;
import carpi.threading.GPSTracking;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

/**
 * Service for tracking the geo location as track / path.
 * 
 * @author osterrath
 *
 */
@ApplicationScoped
public class TrackingService {
	/**
	 * Object for syncing access to the tracking file.
	 */
	private final Object TRACKING_FILE_LOCK = new Object();

	/**
	 * Date format for the CSV tracking files.
	 */
	private final DateFormat TRACKING_FILE_NAME = new SimpleDateFormat("'track-'yyyy-MM-dd_HH-mm'.csv'");

	/**
	 * Date format for the KML tracking files.
	 */
	private final DateFormat KML_FILE_NAME = new SimpleDateFormat("'track-'yyyy-MM-dd_HH-mm'.kml'");

	/**
	 * Date format for KML track title.
	 */
	private final DateFormat KML_TITLE_DATE_FORMAT = new SimpleDateFormat("dd.MMMM.yyyy HH:mm");

	/**
	 * Calculator for geo calculations.
	 */
	private final GeodeticCalculator geoCalculator = new GeodeticCalculator();

	/**
	 * Currently tracked path.
	 */
	private final List<GPSPathElement> currentPath = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Threshold in m for longitude or latitude error values so that we only track "good" values.
	 */
	private double errorThresholdLatLong;

	/**
	 * Threshold in m for altitude error values so that we only track "good" values.
	 */
	private double errorThresholdAlt;

	/**
	 * Threshold in m for detecting movement.
	 */
	private double movementThreshold;

	/**
	 * Threshold in ms for creating new tracking file after a pause.
	 */
	private long pauseThreshold;

	/**
	 * Target directory for creating tracking files.
	 */
	private File targetDir;

	/**
	 * Last found position for detecting movements.
	 */
	private GlobalPosition lastPosition;

	/**
	 * Timestamp (UTC) of last found position.
	 */
	private Long lastPositionTimestamp;

	/**
	 * Current file to write track to.
	 */
	private File currentTrackingFile;

	/**
	 * Executor service for background tasks.
	 */
	@Inject
	@GPSTracking
	private ExecutorService executorService;

	/**
	 * Class logger.
	 */
	@Inject
	private Logger log;

	/**
	 * Application configuration.
	 */
	@Inject
	private CarpiConfiguration config;

	/**
	 * Initializes the tracking service.
	 */
	@PostConstruct
	private void initialize() {
		errorThresholdLatLong = config.getLongitudeLatitudeErrorThreshold();
		errorThresholdAlt = config.getAltitudeErrorThreshold();
		movementThreshold = config.getMovementThreshold();
		pauseThreshold = config.getPauseThreshold();
		String trackingTargetDirecory = config.getTrackingTargetDirecory();
		if (StringUtils.isNotEmpty(trackingTargetDirecory)) {
			targetDir = new File(trackingTargetDirecory);
		} else {
			targetDir = new File(System.getProperty("user.dir"));
		}
	}

	/**
	 * Closes the tracking service.
	 */
	@PreDestroy
	private void destroy() {
		// writes the current track to disc before shutting down
		writeKMLFile(currentPath);
	}

	/**
	 * Event handler when a new GPS position has been received.
	 * 
	 * @param event
	 *            event
	 */
	void onGPSPositionReceived(@Observes GPSPositionChangeEvent event) {
		GPSPosition location = event.getLocation();
		Double longitude = location.getLongitude();
		Double latitude = location.getLatitude();
		Double altitude = location.getAltitude();
		Double timestamp = location.getTimestamp();

		if (longitude == null || latitude == null || altitude == null || timestamp == null) {
			// no valid data
			return;
		}

		// detect too large errors and ignore bad signal
		Double longitudeError = location.getLongitudeError();
		Double latitudeError = location.getLatitudeError();
		Double altitudeError = location.getAltitudeError();
		if (longitudeError != null && latitudeError != null && altitudeError != null) {
			if (longitudeError > errorThresholdLatLong || latitudeError > errorThresholdLatLong) {
				return;
			}
			if (altitudeError > errorThresholdAlt) {
				return;
			}
		}

		long tsLong = epoch2Timestamp(timestamp);
		boolean newTrackingFile = false;
		synchronized (currentPath) {
			// detect pause / no movement
			GlobalPosition currentPosition = new GlobalPosition(latitude, longitude, altitude);
			if (lastPosition != null && lastPositionTimestamp != null) {
				GeodeticMeasurement m = geoCalculator.calculateGeodeticMeasurement(Ellipsoid.WGS84, lastPosition, currentPosition);
				if (m.getPointToPointDistance() < movementThreshold) {
					return;
				}

				newTrackingFile = (tsLong - lastPositionTimestamp) > pauseThreshold;
			}
			lastPosition = currentPosition;
			lastPositionTimestamp = tsLong;

			GPSPathElement el = new GPSPathElement();
			el.setLongitude(longitude);
			el.setLatitude(latitude);
			el.setAltitude(altitude);
			el.setTimestamp(tsLong);
			currentPath.add(el);
		}

		// append to tracking file async
		final boolean newTFFinal = newTrackingFile;
		executorService.execute(() -> appendTrackElementToTrackingFile(location, newTFFinal));

		// create KML file after pause
		if (newTrackingFile) {
			executorService.execute(() -> writeKMLFile(currentPath));
		}
	}

	/**
	 * Appends the given location to the current tracking file.
	 * 
	 * @param location
	 *            location to add
	 * @param newTrackingFile
	 *            flag if we must create new tracking file
	 */
	private void appendTrackElementToTrackingFile(GPSPosition location, boolean newTrackingFile) {
		String line = String.format("%.0f;%f;%f;%.1f%n", location.getTimestamp(), location.getLongitude(), location.getLatitude(), location.getAltitude());

		try {
			synchronized (TRACKING_FILE_LOCK) {
				if (newTrackingFile || currentTrackingFile == null) {
					long tsLong = epoch2Timestamp(location.getTimestamp());
					currentTrackingFile = new File(targetDir, TRACKING_FILE_NAME.format(tsLong));
				}
				FileUtils.write(currentTrackingFile, line, StandardCharsets.UTF_8, true);
			}
		} catch (IOException e) {
			log.log(Level.WARNING, "Could not write tracking file", e);
		}
	}

	/**
	 * Writes the given track as KML file.
	 * 
	 * @param track
	 *            track to write
	 */
	private void writeKMLFile(List<GPSPathElement> track) {
		if (track == null || track.isEmpty()) {
			return;
		}
		long startDate = track.get(0).getTimestamp();
		Kml kml = createKMLObject(track);
		try {
			kml.marshal(new File(targetDir, KML_FILE_NAME.format(startDate)));
		} catch (IOException e) {
			log.log(Level.WARNING, "Could not write KML file", e);
		}
	}

	/**
	 * Creates a KML object from the given track.
	 * 
	 * @param track
	 *            track
	 * @return KML object
	 */
	private Kml createKMLObject(List<GPSPathElement> track) {
		String name;
		if (track.size() == 1) {
			GPSPathElement el = track.get(0);
			long date = el.getTimestamp();
			name = KML_TITLE_DATE_FORMAT.format(date);
		} else {
			GPSPathElement firstEl = track.get(0);
			GPSPathElement lastEl = track.get(track.size() - 1);
			long firstDate = firstEl.getTimestamp();
			long lastDate = lastEl.getTimestamp();
			name = String.format("%s - %s", KML_TITLE_DATE_FORMAT.format(firstDate), KML_TITLE_DATE_FORMAT.format(lastDate));
		}

		Kml kml = new Kml();
		Placemark placemark = kml.createAndSetPlacemark();
		placemark.setName(name);
		LineString lineString = placemark.createAndSetLineString();
		track.stream().forEachOrdered(pe -> lineString.addToCoordinates(pe.getLongitude(), pe.getLatitude(), pe.getAltitude()));
		return kml;
	}

	/**
	 * Getter method for the field "currentPath".
	 *
	 * @return the currentPath
	 */
	public GPSTrack getCurrentPath() {
		synchronized (currentPath) {
			return new GPSTrack(currentPath);
		}
	}

	/**
	 * Writes the current path as KML to the given output stream.
	 * 
	 * @param os
	 *            target output stream
	 * @throws IOException
	 */
	public void writeCurrentPathToStream(OutputStream os) throws IOException {
		createKMLObject(currentPath).marshal(os);
	}

	/**
	 * Converts from the GPS epoch time (float number in s) to Java timestamp (long in ms).
	 * 
	 * @param epoch
	 *            GPS time
	 * @return Java timestamp
	 */
	private long epoch2Timestamp(double epoch) {
		return Double.valueOf(epoch * 1000).longValue();
	}
}
