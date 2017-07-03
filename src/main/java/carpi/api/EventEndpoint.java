package carpi.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

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
	}

}
