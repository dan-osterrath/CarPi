package carpi.model;

import java.util.Date;

/**
 * Model for the daylight data of current position and day.
 * 
 * @author osterrath
 *
 */
public class DaylightData {
	/**
	 * Todays sunrise at current position.
	 */
	private Date sunrise;

	/**
	 * Todays sunset at current position..
	 */
	private Date sunset;

	/**
	 * Flag if its currently day (or night).
	 */
	private boolean day;

	/**
	 * Getter method for the field "sunrise".
	 *
	 * @return the sunrise
	 */
	public Date getSunrise() {
		return sunrise;
	}

	/**
	 * Setter method for the field "sunrise".
	 *
	 * @param sunrise
	 *            the sunrise to set
	 */
	public void setSunrise(Date sunrise) {
		this.sunrise = sunrise;
	}

	/**
	 * Getter method for the field "sunset".
	 *
	 * @return the sunset
	 */
	public Date getSunset() {
		return sunset;
	}

	/**
	 * Setter method for the field "sunset".
	 *
	 * @param sunset
	 *            the sunset to set
	 */
	public void setSunset(Date sunset) {
		this.sunset = sunset;
	}

	/**
	 * Getter method for the field "day".
	 *
	 * @return the day
	 */
	public boolean isDay() {
		return day;
	}

	/**
	 * Setter method for the field "day".
	 *
	 * @param day
	 *            the day to set
	 */
	public void setDay(boolean day) {
		this.day = day;
	}

}
