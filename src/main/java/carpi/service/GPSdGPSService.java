package carpi.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.json.JSONException;

import carpi.config.CarpiConfiguration;
import carpi.event.GPSMetaInfoChangeEvent;
import carpi.event.GPSPositionChangeEvent;
import carpi.model.GPSMetaInfo;
import carpi.model.GPSPosition;
import de.taimos.gpsd4java.api.IObjectListener;
import de.taimos.gpsd4java.backend.GPSdEndpoint;
import de.taimos.gpsd4java.backend.ResultParser;
import de.taimos.gpsd4java.types.ATTObject;
import de.taimos.gpsd4java.types.DeviceObject;
import de.taimos.gpsd4java.types.DevicesObject;
import de.taimos.gpsd4java.types.SKYObject;
import de.taimos.gpsd4java.types.TPVObject;
import de.taimos.gpsd4java.types.subframes.SUBFRAMEObject;

/**
 * Service for receiving geo location from GPS device.
 * 
 * @author osterrath
 *
 */
@ApplicationScoped
public class GPSdGPSService implements GPSService {
	/**
	 * Logger.
	 */
	@Inject
	private Logger log;

	/**
	 * Configuration.
	 */
	@Inject
	private CarpiConfiguration config;

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
	 * Used GPS endpoint.
	 */
	private GPSdEndpoint ep;

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
	 * GPSd object listener.
	 */
	private final IObjectListener listener = new GPSObjectListener();

	/**
	 * Initializes the GPSd endpoint.
	 */
	@PostConstruct
	public void initialize() {
		try {
			ep = new GPSdEndpoint(config.getGPSdHost(), config.getGPSdPort(), new ResultParser());
			ep.addListener(listener);
			ep.start();
			ep.watch(true, true);
		} catch (IOException e) {
			log.log(Level.WARNING, "Could not initialize GPSd endpoint", e);
		}
	}

	/**
	 * Closes the connection to the GPSd endpoint.
	 */
	@PreDestroy
	public void close() {
		if (ep == null) {
			return;
		}
		try {
			ep.watch(false, false);
		} catch (JSONException | IOException e) {
			log.log(Level.WARNING, "Could not shutdown GPSd connection: %s", e);
		}
		ep.stop();
		ep.removeListener(listener);
		ep = null;
	}

	/* (non-Javadoc)
	 * @see carpi.service.GPSService#getLastPosition()
	 */
	@Override
	public GPSPosition getLastPosition() {
		synchronized (LAST_POSITION_LOCK) {
			return lastPosition;
		}
	}

	/* (non-Javadoc)
	 * @see carpi.service.GPSService#getLastMetaInfo()
	 */
	@Override
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

	/**
	 * Listener for handling GPS data objects.
	 * 
	 * @author osterrath
	 *
	 */
	private class GPSObjectListener implements IObjectListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see de.taimos.gpsd4java.api.IObjectListener#handleTPV(de.taimos.gpsd4java.types.TPVObject)
		 */
		@Override
		public void handleTPV(TPVObject tpv) {
			log.log(Level.FINE, "Received TPV {0}", tpv);
			synchronized (LAST_POSITION_LOCK) {
				lastPosition = new GPSPosition();
				lastPosition.setLongitude(mapValue(tpv.getLongitude()));
				lastPosition.setLatitude(mapValue(tpv.getLatitude()));
				lastPosition.setAltitude(mapValue(tpv.getAltitude()));
				lastPosition.setSpeed(mapValue(tpv.getSpeed()));
				lastPosition.setClimbRate(mapValue(tpv.getClimbRate()));
				lastPosition.setLongitudeError(mapValue(tpv.getLongitudeError()));
				lastPosition.setLatitudeError(mapValue(tpv.getLatitudeError()));
				lastPosition.setAltitudeError(mapValue(tpv.getAltitudeError()));
				lastPosition.setSpeedError(mapValue(tpv.getSpeedError()));
				lastPosition.setClimbRateError(mapValue(tpv.getClimbRateError()));
				lastPosition.setTimestamp(mapValue(tpv.getTimestamp()));
			}

			gpsPositionChangeEvent.fire(new GPSPositionChangeEvent(lastPosition));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.taimos.gpsd4java.api.IObjectListener#handleSKY(de.taimos.gpsd4java.types.SKYObject)
		 */
		@Override
		public void handleSKY(SKYObject sky) {
			log.log(Level.FINE, "Received SKY {0}", sky);

			long numSatellites = sky.getSatellites().stream().filter(s -> s.getUsed()).count();
			synchronized (LAST_META_INFO_LOCK) {
				lastMetaInfo = new GPSMetaInfo();
				lastMetaInfo.setNumSatellites((int) numSatellites);
			}

			gpsMetaInfoChangeEvent.fire(new GPSMetaInfoChangeEvent(lastMetaInfo));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.taimos.gpsd4java.api.IObjectListener#handleATT(de.taimos.gpsd4java.types.ATTObject)
		 */
		@Override
		public void handleATT(ATTObject att) {
			log.log(Level.FINE, "Received ATT {0}", att);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.taimos.gpsd4java.api.IObjectListener#handleSUBFRAME(de.taimos.gpsd4java.types.subframes.SUBFRAMEObject)
		 */
		@Override
		public void handleSUBFRAME(SUBFRAMEObject subframe) {
			log.log(Level.FINE, "Received SUBFRAME {0}", subframe);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.taimos.gpsd4java.api.IObjectListener#handleDevices(de.taimos.gpsd4java.types.DevicesObject)
		 */
		@Override
		public void handleDevices(DevicesObject devices) {
			log.log(Level.FINE, "Received Devices {0}", devices);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.taimos.gpsd4java.api.IObjectListener#handleDevice(de.taimos.gpsd4java.types.DeviceObject)
		 */
		@Override
		public void handleDevice(DeviceObject device) {
			log.log(Level.FINE, "Received Device {0}", device);
		}

		/**
		 * Maps a double value to a {@link Double} value. If the given value is {@link Double#NaN}, {@link Double#NEGATIVE_INFINITY} or {@link Double#POSITIVE_INFINITY}
		 * it will return <code>null</code>.
		 * 
		 * @param v
		 *            double value
		 * @return mapped {@link Double} value
		 */
		private Double mapValue(double v) {
			if (Double.isNaN(v) || Double.isInfinite(v)) {
				return null;
			}
			return v;
		}
	}
}
