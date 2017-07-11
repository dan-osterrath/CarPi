package net.packsam.carpi.model;

/**
 * Model for a single path element of GPS track.
 * 
 * @author osterrath
 *
 */
public class GPSPathElement {
	/**
	 * Timestamp (UTC).
	 */
	private long timestamp;
	/**
	 * Longitude in degrees.
	 */
	private double longitude;
	/**
	 * Latitude in degrees.
	 */
	private double latitude;
	/**
	 * Altitude in m.
	 */
	private double altitude;
	/**
	 * Distance to last path element in m.
	 */
	private double distanceToLast;

	/**
	 * Getter method for the field "latitude".
	 *
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Setter method for the field "latitude".
	 *
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * Getter method for the field "altitude".
	 *
	 * @return the altitude
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * Setter method for the field "altitude".
	 *
	 * @param altitude
	 *            the altitude to set
	 */
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	/**
	 * Getter method for the field "timestamp".
	 *
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Setter method for the field "timestamp".
	 *
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Getter method for the field "longitude".
	 *
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Setter method for the field "longitude".
	 *
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * Getter method for the field "distanceToLast".
	 *
	 * @return the distanceToLast
	 */
	public double getDistanceToLast() {
		return distanceToLast;
	}

	/**
	 * Setter method for the field "distanceToLast".
	 *
	 * @param distanceToLast
	 *            the distanceToLast to set
	 */
	public void setDistanceToLast(double distanceToLast) {
		this.distanceToLast = distanceToLast;
	}

}
