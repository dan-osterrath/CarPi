package net.packsam.carpi.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

/**
 * Class for creating executor services.
 * 
 * @author osterrath
 *
 */
@ApplicationScoped
public class ExecutorServiceFactory {
	/**
	 * Creates the executor service for GPS tracking.
	 * 
	 * @return executor service
	 */
	@Produces
	@GPSTracking
	@ApplicationScoped
	public ExecutorService createGPSTrackingExecutorService() {
		ExecutorService es = Executors.newSingleThreadExecutor();
		return es;
	}

	/**
	 * Shuts down the executor service for GPS tracking.
	 * 
	 * @param es
	 *            executor service
	 */
	public void disposeGPSTrackingExecutorService(@Disposes @GPSTracking ExecutorService es) {
		es.shutdown();
	}

	/**
	 * Creates the executor service for health monitoring.
	 * 
	 * @return executor service
	 */
	@Produces
	@HealthMonitor
	@ApplicationScoped
	public ScheduledExecutorService createHealthMonitorExecutorService() {
		ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
		return es;
	}

	/**
	 * Shuts down the executor service for health monitoring.
	 * 
	 * @param es
	 *            executor service
	 */
	public void disposeHealthMonitorExecutorService(@Disposes @HealthMonitor ScheduledExecutorService es) {
		es.shutdown();
	}

	/**
	 * Creates the executor service for daylight monitoring.
	 * 
	 * @return executor service
	 */
	@Produces
	@DaylightMonitor
	@ApplicationScoped
	public ScheduledExecutorService createDaylightMonitorExecutorService() {
		ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
		return es;
	}

	/**
	 * Shuts down the executor service for daylight monitoring.
	 * 
	 * @param es
	 *            executor service
	 */
	public void disposeDaylightMonitorExecutorService(@Disposes @DaylightMonitor ScheduledExecutorService es) {
		es.shutdown();
	}

	/**
	 * Creates the executor service for car data monitoring.
	 * 
	 * @return executor service
	 */
	@Produces
	@CarMonitor
	@ApplicationScoped
	public ScheduledExecutorService createCarDataMonitorExecutorService() {
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		executor.setRemoveOnCancelPolicy(true);
		return Executors.unconfigurableScheduledExecutorService(executor);
	}

	/**
	 * Shuts down the executor service for car data monitoring.
	 * 
	 * @param es
	 *            executor service
	 */
	public void disposeCarDataMonitorExecutorService(@Disposes @CarMonitor ScheduledExecutorService es) {
		es.shutdown();
	}
}
