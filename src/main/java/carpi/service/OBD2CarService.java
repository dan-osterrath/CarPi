package carpi.service;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import carpi.config.CarpiConfiguration;
import carpi.model.CarData;
import carpi.threading.CarMonitor;

/**
 * Service for reading cara data from OBD2 dongle.
 * 
 * @author osterrath
 *
 */
public class OBD2CarService implements CarService {

	/**
	 * Last read car data.
	 */
	private CarData lastCarData;

	/**
	 * Lock object for handling {@link OBD2CarService#lastCarData}.
	 */
	private final Object LAST_CAR_DATA_LOCK = new Object();

	/**
	 * Flag if the ODB2 dongle is connected.
	 */
	private boolean isConnected = false;

	/**
	 * Lock object for handling {@link OBD2CarService#isConnected}.
	 */
	private final Object IS_CONNECTED_LOCK = new Object();

	/**
	 * Executor service for running monitor in background.
	 */
	@Inject
	@CarMonitor
	private ScheduledExecutorService executorService;

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
	 * Task for connecting.
	 */
	private ScheduledFuture<?> connectTask;
	
	/**
	 * Task for reading data.
	 */
	private ScheduledFuture<?> readTask;

	/**
	 * Initializes the service.
	 */
	@PostConstruct
	private void initialize() {
		connectTask = executorService.scheduleAtFixedRate(() -> connect(), 0, 10, TimeUnit.SECONDS);
	}

	/**
	 * Tries to connect to the ODB2 dongle.
	 */
	private void connect() {
		// TODO
		log.info("Connecting...");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			log.log(Level.INFO, "cancelled", e);
		}

		synchronized (IS_CONNECTED_LOCK) {
			this.isConnected = true;
		}

		connectTask.cancel(false);
		readTask = executorService.scheduleAtFixedRate(() -> readData(), 0, 1, TimeUnit.SECONDS);
	}

	/**
	 * Reads current car data from ODB2 dongle.
	 */
	private void readData() {
		log.info("Reading data...");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			log.log(Level.INFO, "cancelled", e);
		}
		
		if (Math.random() > 0.5) {
			synchronized (IS_CONNECTED_LOCK) {
				isConnected = false;
			}
			readTask.cancel(false);
			connectTask = executorService.scheduleAtFixedRate(() -> connect(), 0, 10, TimeUnit.SECONDS);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see carpi.service.CarService#getCarData()
	 */
	@Override
	public CarData getCarData() {
		synchronized (LAST_CAR_DATA_LOCK) {
			return lastCarData;
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
