package carpi.service;

import static carpi.service.Jitter.getJitter;

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

import carpi.event.HealthStatusChangeEvent;
import carpi.model.HealthStatus;

/**
 * Service for checking the system health. This service fakes all data.
 * 
 * @author osterrath
 *
 */
@ApplicationScoped
@Alternative
public class MockHealthService implements HealthService {

	/**
	 * Event when the health status has been changed.
	 */
	@Inject
	private Event<HealthStatusChangeEvent> healthStatusChangeEvent;

	/**
	 * Currently found health status.
	 */
	private final HealthStatus currentHealthStatus = new HealthStatus();

	/**
	 * Executor service for faking incoming GPS signals.
	 */
	private ScheduledExecutorService es;

	/**
	 * Initializes thread executor service.
	 */
	@PostConstruct
	public void initialize() {
		es = Executors.newSingleThreadScheduledExecutor();
		es.scheduleAtFixedRate(this::fakeHealthStatus, 0, 1, TimeUnit.SECONDS);
	}

	/**
	 * Closes the thread executor service.
	 */
	@PreDestroy
	public void close() {
		es.shutdownNow();
	}

	/**
	 * Fakes the current health status.
	 */
	private void fakeHealthStatus() {
		synchronized (currentHealthStatus) {
			if (currentHealthStatus.getDiscTotal() == 0) {
				currentHealthStatus.setDiscTotal(68719476736L); // 64GB
			}
			if (currentHealthStatus.getMemTotal() == 0) {
				currentHealthStatus.setMemTotal(1073741824); // 1GB
			}
			currentHealthStatus.setGpuTemperature(getJitter(55, 10));
			currentHealthStatus.setCpuTemperature(getJitter(55, 10));
			currentHealthStatus.setCpuUsage(Math.min(Math.max(0, getJitter(currentHealthStatus.getCpuUsage(), 10)), 100));
			if (currentHealthStatus.getCpuVoltage() == 0) {
				currentHealthStatus.setCpuVoltage(1.2);
			}
			currentHealthStatus.setCpuVoltage((Math.min(Math.max(1, getJitter(currentHealthStatus.getCpuVoltage(), 0.1)), 1.6)));
			currentHealthStatus.setDiscFree((long) getJitter(currentHealthStatus.getDiscTotal() * 0.8, currentHealthStatus.getDiscTotal() * 0.2));
			currentHealthStatus.setMemFree((long) getJitter(currentHealthStatus.getMemTotal() * 0.8, currentHealthStatus.getMemTotal() * 0.2));
			currentHealthStatus.setSystemLoad(Math.min(Math.max(0, getJitter(currentHealthStatus.getSystemLoad(), 0.2)), 1));
			currentHealthStatus.setBatteryVoltage(getJitter(5, 1));
			currentHealthStatus.setInputVoltage(getJitter(5, 1));
		}

		healthStatusChangeEvent.fire(new HealthStatusChangeEvent(currentHealthStatus));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see carpi.service.HealthService#getHealthStatus()
	 */
	@Override
	public HealthStatus getHealthStatus() {
		synchronized (currentHealthStatus) {
			return currentHealthStatus;
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
