package carpi.api;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.websocket.Session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import carpi.event.DaylightDataChangeEvent;
import carpi.event.GPSMetaInfoChangeEvent;
import carpi.event.GPSPositionChangeEvent;
import carpi.event.HealthStatusChangeEvent;
import carpi.model.EventMessage;

/**
 * CDI event handler for dispatching the change events to all websocket clients.
 * 
 * @author osterrath
 *
 */
@ApplicationScoped
public class EventEndpointEventHandler {

	/**
	 * Class logger.
	 */
	@Inject
	private Logger log;
	
	/**
	 * JSON object mapper.
	 */
	private ObjectMapper objectMapper;

	/**
	 * Initializes the event handler.
	 */
	@PostConstruct
	private void initialize() {
		objectMapper = new ObjectMapper();
	}
	
	/**
	 * Sends an event to all registered clients.
	 * 
	 * @param event
	 *            event to send
	 */
	private void sendEventToClients(Object event) {
		synchronized (EventEndpoint.clientSessions) {
			if (EventEndpoint.clientSessions.isEmpty()) {
				return;
			}
			String type = event.getClass().getSimpleName();

			EventMessage evtMsg = new EventMessage();
			evtMsg.setType(type);
			evtMsg.setEvent(event);
			
			String msg;
			try {
				msg = objectMapper.writeValueAsString(evtMsg);
			} catch (JsonProcessingException e) {
				log.log(Level.WARNING, "Could not encode message", e);
				return;
			}

			EventEndpoint.clientSessions.forEach(s -> sendMessageToClient(msg, s));
		}
	}

	/**
	 * Sends the given message to the given client session.
	 * 
	 * @param msg
	 *            message to send
	 * @param session
	 *            client session
	 * @return future for the result
	 */
	private void sendMessageToClient(String msg, Session session) {
		try {
			session.getBasicRemote().sendText(msg);
		} catch (IOException e) {
			log.log(Level.WARNING, "Could not send message", e);
		}
	}

	/**
	 * Event handler when the GPS position has been changed.
	 * 
	 * @param e
	 *            change event
	 */
	public void onGPSPositionChanged(@Observes GPSPositionChangeEvent e) {
		sendEventToClients(e);
	}

	/**
	 * Event handler when the daylight data has been changed.
	 * 
	 * @param e
	 *            change event
	 */
	public void onDaylightDataChanged(@Observes DaylightDataChangeEvent e) {
		sendEventToClients(e);
	}

	/**
	 * Event handler when the GPS meta data have been changed.
	 * 
	 * @param e
	 *            change event
	 */
	public void onGPSMetaInfoChanged(@Observes GPSMetaInfoChangeEvent e) {
		sendEventToClients(e);
	}

	/**
	 * Event handler when the health status has been changed.
	 * 
	 * @param e
	 *            change event
	 */
	public void onHealthStatusChanged(@Observes HealthStatusChangeEvent e) {
		sendEventToClients(e);
	}

}
