/**
 * (C) 2017 by 3m5. Media GmbH. http://www.3m5.de
 */
package carpi.model;

/**
 * Model class for the current GPS position.
 * 
 * @author osterrath
 *
 */
public class GPSPosition {
	/**
	 * Latitude in degrees. +/- signifies North/South.
	 */
	private Double latitude;
	/**
	 * Longitude in degrees. +/- signifies East/West.
	 */
	private Double longitude;
	/**
	 * Altitude in m.
	 */
	private Double altitude;
	/**
	 * Latitude error in m.
	 */
	private Double latitudeError;
	/**
	 * Longitude error in m.
	 */
	private Double longitudeError;
	/**
	 * Altitude error in m.
	 */
	private Double altitudeError;
	/**
	 * Speed in m/s.
	 */
	private Double speed;
	/**
	 * Climb rate in m/s.
	 */
	private Double climbRate;
	/**
	 * Speed error in m/s.
	 */
	private Double speedError;
	/**
	 * Climb rate error in m/s.
	 */
	private Double climbRateError;
	/**
	 * Current timestamp.
	 */
	private Double timestamp;

	/**
	 * Getter method for the field "latitude".
	 *
	 * @return the latitude
	 */
	public Double getLatitude() {
		return latitude;
	}

	/**
	 * Setter method for the field "latitude".
	 *
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	/**
	 * Getter method for the field "longitude".
	 *
	 * @return the longitude
	 */
	public Double getLongitude() {
		return longitude;
	}

	/**
	 * Setter method for the field "longitude".
	 *
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	/**
	 * Getter method for the field "altitude".
	 *
	 * @return the altitude
	 */
	public Double getAltitude() {
		return altitude;
	}

	/**
	 * Setter method for the field "altitude".
	 *
	 * @param altitude
	 *            the altitude to set
	 */
	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}

	/**
	 * Getter method for the field "latitudeError".
	 *
	 * @return the latitudeError
	 */
	public Double getLatitudeError() {
		return latitudeError;
	}

	/**
	 * Setter method for the field "latitudeError".
	 *
	 * @param latitudeError
	 *            the latitudeError to set
	 */
	public void setLatitudeError(Double latitudeError) {
		this.latitudeError = latitudeError;
	}

	/**
	 * Getter method for the field "longitudeError".
	 *
	 * @return the longitudeError
	 */
	public Double getLongitudeError() {
		return longitudeError;
	}

	/**
	 * Setter method for the field "longitudeError".
	 *
	 * @param longitudeError
	 *            the longitudeError to set
	 */
	public void setLongitudeError(Double longitudeError) {
		this.longitudeError = longitudeError;
	}

	/**
	 * Getter method for the field "altitudeError".
	 *
	 * @return the altitudeError
	 */
	public Double getAltitudeError() {
		return altitudeError;
	}

	/**
	 * Setter method for the field "altitudeError".
	 *
	 * @param altitudeError
	 *            the altitudeError to set
	 */
	public void setAltitudeError(Double altitudeError) {
		this.altitudeError = altitudeError;
	}

	/**
	 * Getter method for the field "speed".
	 *
	 * @return the speed
	 */
	public Double getSpeed() {
		return speed;
	}

	/**
	 * Setter method for the field "speed".
	 *
	 * @param speed
	 *            the speed to set
	 */
	public void setSpeed(Double speed) {
		this.speed = speed;
	}

	/**
	 * Getter method for the field "climbRate".
	 *
	 * @return the climbRate
	 */
	public Double getClimbRate() {
		return climbRate;
	}

	/**
	 * Setter method for the field "climbRate".
	 *
	 * @param climbRate
	 *            the climbRate to set
	 */
	public void setClimbRate(Double climbRate) {
		this.climbRate = climbRate;
	}

	/**
	 * Getter method for the field "speedError".
	 *
	 * @return the speedError
	 */
	public Double getSpeedError() {
		return speedError;
	}

	/**
	 * Setter method for the field "speedError".
	 *
	 * @param speedError
	 *            the speedError to set
	 */
	public void setSpeedError(Double speedError) {
		this.speedError = speedError;
	}

	/**
	 * Getter method for the field "climbRateError".
	 *
	 * @return the climbRateError
	 */
	public Double getClimbRateError() {
		return climbRateError;
	}

	/**
	 * Setter method for the field "climbRateError".
	 *
	 * @param climbRateError
	 *            the climbRateError to set
	 */
	public void setClimbRateError(Double climbRateError) {
		this.climbRateError = climbRateError;
	}

	/**
	 * Getter method for the field "timestamp".
	 *
	 * @return the timestamp
	 */
	public Double getTimestamp() {
		return timestamp;
	}

	/**
	 * Setter method for the field "timestamp".
	 *
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(Double timestamp) {
		this.timestamp = timestamp;
	}

}
