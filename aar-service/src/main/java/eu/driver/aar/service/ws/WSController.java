package eu.driver.aar.service.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class WSController {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private static WSController aMe;
	
	private Map<String, WebSocketSession> sessionMap = new HashMap<String, WebSocketSession>();
	
	private WSController() {
		log.info("WSController");
	}
	
	public static synchronized WSController getInstance() {
		if (WSController.aMe == null) {
			WSController.aMe = new WSController();
		}
		return WSController.aMe;
	}
	
	public void addWSSession(WebSocketSession session) {
		log.debug("--> addWSSession");
		sessionMap.put(session.getId(), session);
		log.debug("addWSSession -->");
	}
	
	public void removeWSSession(String id) {
		log.debug("--> addWSSession");
		sessionMap.remove(id);
		log.debug("addWSSession -->");
	}
	
	public synchronized void sendMessage(String msg) {
		log.debug("--> sendMessage");
		log.debug(msg);
		TextMessage responseMsg = new TextMessage(msg);
		try {
			for (Map.Entry<String, WebSocketSession> pair : sessionMap.entrySet()) {
				WebSocketSession session = pair.getValue();
				if (session != null && session.isOpen()) {
					session.sendMessage(responseMsg);
				}
			}
		} catch (IOException e) {
			log.error("Error sending the notification!");
		}
		log.debug("sendMessage -->");
	}

}
