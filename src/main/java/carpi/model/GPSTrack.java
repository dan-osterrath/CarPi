package carpi.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Model class for a GPS track.
 * 
 * @author osterrath
 *
 */
public class GPSTrack {

	/**
	 * Start of track as timestamp.
	 */
	private long start;

	/**
	 * Distance in m.
	 */
	private long distance;

	/**
	 * Path of GPS track.
	 */
	private List<Element> path;

	/**
	 * Ctor.
	 *
	 */
	public GPSTrack() {
		super();
	}

	/**
	 * Ctor.
	 * 
	 * @param path
	 *            GPS path elements
	 */
	public GPSTrack(Collection<GPSPathElement> path) {
		super();
		if (!path.isEmpty()) {
			start = path.iterator().next().getTimestamp();
			this.path = path.stream() //
					.map(e -> new Element(e)) //
					.collect(Collectors.toList());
			distance = Math.round(//
					path.stream() //
							.collect(Collectors.summarizingDouble(GPSPathElement::getDistanceToLast)) //
							.getSum() //
					);
		} else {
			start = System.currentTimeMillis();
			distance = 0;
		}

	}

	/**
	 * Getter method for the field "start".
	 *
	 * @return the start
	 */
	public long getStart() {
		return start;
	}

	/**
	 * Setter method for the field "start".
	 *
	 * @param start
	 *            the start to set
	 */
	public void setStart(long start) {
		this.start = start;
	}

	/**
	 * Getter method for the field "distance".
	 *
	 * @return the distance
	 */
	public long getDistance() {
		return distance;
	}

	/**
	 * Setter method for the field "distance".
	 *
	 * @param distance
	 *            the distance to set
	 */
	public void setDistance(long distance) {
		this.distance = distance;
	}

	/**
	 * Getter method for the field "path".
	 *
	 * @return the path
	 */
	public List<Element> getPath() {
		return path;
	}

	/**
	 * Setter method for the field "path".
	 *
	 * @param path
	 *            the path to set
	 */
	public void setPath(List<Element> path) {
		this.path = path;
	}

	/**
	 * Model class for a GPS track element.
	 * 
	 * @author osterrath
	 *
	 */
	protected static class Element {
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
		 * Ctor.
		 *
		 */
		public Element() {
			super();
		}

		/**
		 * Ctor.
		 *
		 */
		public Element(GPSPathElement e) {
			super();
			longitude = e.getLongitude();
			latitude = e.getLatitude();
			altitude = e.getAltitude();
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

	}
}
