package net.packsam.carpi.service;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import net.packsam.carpi.event.DaylightDataChangeEvent;
import net.packsam.carpi.model.DaylightData;
import net.packsam.carpi.model.GPSPosition;
import net.packsam.carpi.threading.DaylightMonitor;

/**
 * Service for calculating sunrise, sunset and current daylight status.
 * 
 * @author osterrath
 *
 */
@ApplicationScoped
public class DaylightService {

	/**
	 * Current daylight data.
	 */
	private final DaylightData currentData = new DaylightData();

	/**
	 * GPS service for getting current position.
	 */
	@Inject
	private GPSService gpsService;

	/**
	 * Event when the health status has been changed.
	 */
	@Inject
	private Event<DaylightDataChangeEvent> daylightDataChangeEvent;

	/**
	 * Executor service for running monitor in background.
	 */
	@Inject
	@DaylightMonitor
	private ScheduledExecutorService executorService;

	/**
	 * Initializes the service.
	 */
	@PostConstruct
	private void initialize() {
		updateData();
	}

	/**
	 * Updates the data for the current position.
	 */
	private void updateData() {
		GPSPosition position = gpsService.getLastPosition();
		if (position == null) {
			// no position yet
			return;
		}

		Calendar now = Calendar.getInstance();
		SunriseSunsetCalculator c = new SunriseSunsetCalculator(new Location(position.getLatitude(), position.getLongitude()), now.getTimeZone());
		Date sunrise = c.getOfficialSunriseCalendarForDate(now).getTime();
		Date sunset = c.getOfficialSunsetCalendarForDate(now).getTime();
		boolean day = now.after(currentData.getSunrise()) && now.before(currentData.getSunset());

		boolean changed = false;
		synchronized (currentData) {
			if (!sunrise.equals(currentData.getSunrise())) {
				currentData.setSunrise(sunrise);
				changed = true;
			}
			if (!sunset.equals(currentData.getSunset())) {
				currentData.setSunset(sunset);
				changed = true;
			}
			if (day != currentData.isDay()) {
				currentData.setDay(day);
				changed = true;
			}
		}

		if (changed) {
			daylightDataChangeEvent.fire(new DaylightDataChangeEvent(currentData));
		}

		// find next date to check daylight data
		Date nextDate;
		if (day) {
			// next date to check in sunset
			nextDate = sunset;
		} else if (now.getTime().before(sunrise)) {
			// next date to check is sunrise
			nextDate = sunrise;
		} else {
			// next date to check is midnight
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 10);
			cal.set(Calendar.MILLISECOND, 0);
			nextDate = cal.getTime();
		}

		// check at least in 1h again (due to changing GPS position)
		executorService.schedule(() -> updateData(), Math.min(nextDate.getTime() - System.currentTimeMillis(), 3600000L), TimeUnit.MILLISECONDS);
	}

	/**
	 * Returns the current daylight data.
	 * 
	 * @return daylight data
	 */
	public DaylightData getDaylightData() {
		synchronized (currentData) {
			return currentData;
		}
	}
}
