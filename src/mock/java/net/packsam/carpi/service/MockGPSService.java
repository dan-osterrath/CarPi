package net.packsam.carpi.service;

import static net.packsam.carpi.service.Jitter.getJitter;

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

import net.packsam.carpi.event.GPSMetaInfoChangeEvent;
import net.packsam.carpi.event.GPSPositionChangeEvent;
import net.packsam.carpi.model.GPSMetaInfo;
import net.packsam.carpi.model.GPSPosition;

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
	 * Lock object for handling {@link MockGPSService#lastPosition}.
	 */
	private final Object LAST_POSITION_LOCK = new Object();

	/**
	 * Lock object for handling {@link MockGPSService#lastMetaInfo}.
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
		if (lastMetaInfo == null || lastMetaInfo.getNumSatellites() == 0) {
			return;
		}
		synchronized (LAST_POSITION_LOCK) {
			if (lastPosition == null) {
				lastPosition = new GPSPosition();
				lastPosition.setLongitude(getJitter(13.4247317, 0.05));
				lastPosition.setLatitude(getJitter(52.5068441, 0.05));
				lastPosition.setAltitude(getJitter(100, 10));
				lastPosition.setSpeed(0.0);
				lastPosition.setClimbRate(0.0);
			} else {
				lastPosition.setLongitude(getJitter(lastPosition.getLongitude(), 0.00025));
				lastPosition.setLatitude(getJitter(lastPosition.getLatitude(), 0.00025));
				lastPosition.setAltitude(Math.max(0, getJitter(lastPosition.getAltitude(), 0.5)));
				lastPosition.setSpeed(Math.max(0, getJitter(lastPosition.getSpeed(), 2.0 / 3.6)));
				lastPosition.setClimbRate(Math.max(0, getJitter(lastPosition.getClimbRate(), 0.5 / 3.6)));
			}
			lastPosition.setLongitudeError(20 * Math.random());
			lastPosition.setLatitudeError(20 * Math.random());
			lastPosition.setAltitudeError(80 * Math.random());
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
