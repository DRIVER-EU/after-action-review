package eu.driver.aar.service.ws;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import eu.driver.aar.service.ws.mapper.StringJSONMapper;
import eu.driver.aar.service.ws.object.WSHeartbeatRequest;

@Component
public class WebSocketServer extends TextWebSocketHandler {
	
	private Logger log = Logger.getLogger(this.getClass());
	private StringJSONMapper mapper = new StringJSONMapper();
	
	public WebSocketServer() {

	}
	 
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    	log.info("The WebSocket has been closed!");
    	log.info("Socket closed: " + status.getCode());
    	session.close();
    	WSController.getInstance().setWSSession(null)
    	;
    }
 
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    	log.info("The WebSocket has been opened!");
    	WSController.getInstance().setWSSession(session);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
    	log.debug("Message received: " + textMessage.getPayload());
    	
    	String message = textMessage.getPayload();
    	if (message.indexOf("HBREQUEST") != -1) {
    		WSHeartbeatRequest hbRequest = mapper.stringToHBRequestMessage(textMessage.getPayload());
    		
    		TextMessage responseMsg = new TextMessage(mapper.objectToJSONString(hbRequest.createResponse()));
    		session.sendMessage(responseMsg);
    	}
    }
}
