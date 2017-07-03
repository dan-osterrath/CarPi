package carpi.model;

/**
 * Model class for an event message sent via websocket.
 * 
 * @author osterrath
 *
 */
public class EventMessage {
	/**
	 * Event type.
	 */
	private String type;
	/**
	 * Event.
	 */
	private Object event;

	/**
	 * Getter method for the field "type".
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Setter method for the field "type".
	 *
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Getter method for the field "event".
	 *
	 * @return the event
	 */
	public Object getEvent() {
		return event;
	}

	/**
	 * Setter method for the field "event".
	 *
	 * @param event
	 *            the event to set
	 */
	public void setEvent(Object event) {
		this.event = event;
	}

}
