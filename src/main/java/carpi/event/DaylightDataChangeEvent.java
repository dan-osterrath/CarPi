package carpi.event;

import carpi.model.DaylightData;

/**
 * Event when the daylight data changed.
 * 
 * @author osterrath
 *
 */
public class DaylightDataChangeEvent {
	/**
	 * Current daylight data.
	 */
	private final DaylightData data;

	/**
	 * Ctor.
	 *
	 * @param data
	 *            new daylight data
	 */
	public DaylightDataChangeEvent(DaylightData data) {
		super();
		this.data = data;
	}

	/**
	 * Getter method for the field "data".
	 *
	 * @return the data
	 */
	public DaylightData getData() {
		return data;
	}

}
