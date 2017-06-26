/**
 * (C) 2017 by 3m5. Media GmbH. http://www.3m5.de
 */
package carpi.event;

import carpi.model.HealthStatus;

/**
 * Event when the health status changed.
 * 
 * @author osterrath
 *
 */
public class HealthStatusChangeEvent {
	/**
	 * Current health status.
	 */
	private final HealthStatus status;

	/**
	 * Ctor.
	 *
	 * @param status
	 *            new health status
	 */
	public HealthStatusChangeEvent(HealthStatus status) {
		super();
		this.status = status;
	}

	/**
	 * Getter method for the field "status".
	 *
	 * @return the status
	 */
	public HealthStatus getStatus() {
		return status;
	}

}
