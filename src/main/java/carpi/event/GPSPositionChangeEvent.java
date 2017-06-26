package carpi.event;

import carpi.model.GPSPosition;

/**
 * Event when the GPS position changed.
 * 
 * @author osterrath
 *
 */
public class GPSPositionChangeEvent {
	/**
	 * Current GPS position.
	 */
	private final GPSPosition location;

	/**
	 * Ctor.
	 *
	 * @param position
	 *            new GPS position
	 */
	public GPSPositionChangeEvent(GPSPosition position) {
		super();
		this.location = position;
	}

	/**
	 * Getter method for the field "location".
	 *
	 * @return the location
	 */
	public GPSPosition getLocation() {
		return location;
	}

}
