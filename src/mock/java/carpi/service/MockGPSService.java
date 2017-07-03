package carpi.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import carpi.event.GPSMetaInfoChangeEvent;
import carpi.event.GPSPositionChangeEvent;
import carpi.model.GPSMetaInfo;
import carpi.model.GPSPosition;

/**
 * Service for receiving geo location from GPS device. This service fakes a GPS connection.
 * 
 * @author osterrath
 *
 */
@ApplicationScoped
@Alternative
public class MockGPSService implements GPSService {

	/**
	 * Event when the GPS position has been changed.
	 */
	@Inject
	private Event<GPSPositionChangeEvent> gpsPositionChangeEvent;

	/**
	 * Event when the GPS position has been changed.
	 */
	@Inject
	private Event<GPSMetaInfoChangeEvent> gpsMetaInfoChangeEvent;

	/**
	 * Executor service for faking incoming GPS signals.
	 */
	private ScheduledExecutorService es;

	/**
	 * Last received position.
	 */
	private GPSPosition lastPosition;

	/**
	 * Last received meta info.
	 */
	private GPSMetaInfo lastMetaInfo;

	/**
	 * Lock object for handling {@link GPSdGPSService#lastPosition}.
	 */
	private final Object LAST_POSITION_LOCK = new Object();

	/**
	 * Lock object for handling {@link GPSdGPSService#lastMetaInfo}.
	 */
	private final Object LAST_META_INFO_LOCK = new Object();

	/**
	 * Initializes thread executor service.
	 */
	@PostConstruct
	public void initialize() {
		es = Executors.newSingleThreadScheduledExecutor();
		es.scheduleAtFixedRate(this::fakeIncomingMetaInfo, 10, 1, TimeUnit.SECONDS);
		es.scheduleAtFixedRate(this::fakeIncomingGPSPoition, 10, 1, TimeUnit.SECONDS);
	}

	/**
	 * Closes the thread executor service.
	 */
	@PreDestroy
	public void close() {
		es.shutdownNow();
	}

	/**
	 * Creates a new GPS signal.
	 */
	private void fakeIncomingGPSPoition() {
		synchronized (LAST_POSITION_LOCK) {
			if (lastPosition == null) {
				lastPosition = new GPSPosition();
				lastPosition.setLongitude(13.4247317 + getJitter(0.05));
				lastPosition.setLatitude(52.5068441 + getJitter(0.05));
				lastPosition.setAltitude(100 + getJitter(10));
				lastPosition.setSpeed(0.0);
				lastPosition.setClimbRate(0.0);
			} else {
				lastPosition.setLongitude(lastPosition.getLongitude() + getJitter(0.00025));
				lastPosition.setLatitude(lastPosition.getLatitude() + getJitter(0.00025));
				lastPosition.setAltitude(Math.max(0, lastPosition.getAltitude() + getJitter(0.5)));
				lastPosition.setSpeed(Math.max(0, lastPosition.getSpeed() + getJitter(2.0 / 3.6)));
				lastPosition.setClimbRate(Math.max(0, lastPosition.getClimbRate() + getJitter(0.5 / 3.6)));
			}
			lastPosition.setLongitudeError(30 * Math.random());
			lastPosition.setLatitudeError(30 * Math.random());
			lastPosition.setAltitudeError(30 * Math.random());
			lastPosition.setSpeedError(5 * Math.random());
			lastPosition.setClimbRateError(5 * Math.random());
			lastPosition.setTimestamp(((double) System.currentTimeMillis()) / 1000);
		}

		gpsPositionChangeEvent.fire(new GPSPositionChangeEvent(lastPosition));
	}

	/**
	 * Creates a new meta info object.
	 */
	private void fakeIncomingMetaInfo() {
		synchronized (LAST_META_INFO_LOCK) {
			if (lastMetaInfo == null) {
				lastMetaInfo = new GPSMetaInfo();
				lastMetaInfo.setNumSatellites((int) (Math.random() * 8) + 2);
			} else {
				lastMetaInfo.setNumSatellites(Math.max(0, (int) (lastMetaInfo.getNumSatellites() + getJitter(2))));
			}
		}

		gpsMetaInfoChangeEvent.fire(new GPSMetaInfoChangeEvent(lastMetaInfo));
	}

	/**
	 * Returns some random jitter.
	 * 
	 * @param jitterSize
	 *            jitter size
	 * @return random jitter
	 */
	private double getJitter(double jitterSize) {
		return Math.random() * jitterSize * 2 - jitterSize;
	}

	/**
	 * Getter method for the field "lastPosition".
	 *
	 * @return the lastPosition
	 */
	public GPSPosition getLastPosition() {
		synchronized (LAST_POSITION_LOCK) {
			return lastPosition;
		}
	}

	/**
	 * Getter method for the field "lastMetaInfo".
	 *
	 * @return the lastMetaInfo
	 */
	public GPSMetaInfo getLastMetaInfo() {
		synchronized (LAST_META_INFO_LOCK) {
			return lastMetaInfo;
		}
	}

	/**
	 * Method for implementing @Startup with CDI 1.2.
	 * 
	 * @param init
	 *            init object
	 */
	void onInitApp(@Observes @Initialized(ApplicationScoped.class) Object init) {
	}

}
