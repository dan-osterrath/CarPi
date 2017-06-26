package carpi.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import carpi.model.GPSTrack.Element;

/**
 * Model class for a GPS track.
 * 
 * @author osterrath
 *
 */
public class GPSTrack extends ArrayList<Element> {

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
		super(path.stream().map(e -> {
			Element el = new Element();
			el.setLatitude(e.getLatitude());
			el.setLongitude(e.getLongitude());
			return el;
		}).collect(Collectors.toList()));
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
	}
}
