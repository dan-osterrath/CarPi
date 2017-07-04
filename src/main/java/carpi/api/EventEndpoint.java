package carpi.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Websocket endpoint for sending events to the client. CDI does not work here so we have to share the sessions list as package private collection and use it in an own event
 * handler class.
 * 
 * @see https://issues.jboss.org/browse/WELD-1994
 * 
 * @author osterrath
 *
 */
@ServerEndpoint("/events")
public class EventEndpoint {

	/**
	 * Class logger. CDI injection does not work here.
	 */
	private Logger log = Logger.getLogger(EventEndpoint.class.getName());

	/**
	 * All connected client sessions.
	 */
	final static List<Session> clientSessions = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Subscriptions of all connected clients.
	 */
	final static Map<String, List<Session>> subscriptions = Collections.synchronizedMap(new HashMap<>());

	/**
	 * JSON object mapper.
	 */
	final static ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Handler for opening the session.
	 * 
	 * @param session
	 *            client session
	 */
	@OnOpen
	public void onOpen(Session session) {
		synchronized (clientSessions) {
			clientSessions.add(session);
		}
	}

	/**
	 * Handler for closing the session.
	 * 
	 * @param session
	 *            client session
	 */
	@OnClose
	public void onClose(Session session) {
		synchronized (clientSessions) {
			clientSessions.remove(session);
		}
		synchronized (subscriptions) {
			subscriptions.entrySet().forEach(e -> e.getValue().remove(session));
		}
	}

	/**
	 * Handler for receiving a message from the client.
	 * 
	 * @param message
	 *            message
	 * @param session
	 *            client session
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		log.info("Received message from client: " + message);
		Map<String, String> msg;
		try {
			msg = objectMapper.readValue(message, new TypeReference<HashMap<String, String>>() {
			});
		} catch (IOException e) {
			log.log(Level.INFO, "Could not parse message", e);
			return;
		}
		String event;
		if (StringUtils.isNotEmpty(event = msg.get("SUBSCRIBE"))) {
			synchronized (subscriptions) {
				List<Session> eventSubscriptions = subscriptions.get(event);
				if (eventSubscriptions == null) {
					eventSubscriptions = new ArrayList<>();
					subscriptions.put(event, eventSubscriptions);
				}
				if (!eventSubscriptions.contains(session)) {
					eventSubscriptions.add(session);
				}
			}
		} else if (StringUtils.isNotEmpty(event = msg.get("UNSUBSCRIBE"))) {
			synchronized (subscriptions) {
				List<Session> eventSubscriptions = subscriptions.get(event);
				if (eventSubscriptions != null && eventSubscriptions.contains(session)) {
					eventSubscriptions.remove(session);
				}
			}
		}
	}

}
