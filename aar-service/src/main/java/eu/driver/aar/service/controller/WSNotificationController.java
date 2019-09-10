package eu.driver.aar.service.controller;

import java.util.Date;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketHandler;

import eu.driver.aar.service.ws.WSController;
import eu.driver.aar.service.ws.WebSocketServer;
import eu.driver.aar.service.ws.mapper.StringJSONMapper;
import eu.driver.aar.service.ws.object.WSRecordNotification;
import eu.driver.adapter.constants.TopicConstants;
import eu.driver.model.core.Level;
import eu.driver.model.core.Log;
import eu.driver.model.core.TopicInvite;

@RestController
public class WSNotificationController {
	
	private Logger log = Logger.getLogger(this.getClass());
	private StringJSONMapper mapper = new StringJSONMapper();
	
	@Bean
    public WebSocketHandler wsHandler() {
        return new WebSocketServer();
    }
	
	public WSNotificationController() {

	}
	
	@ApiOperation(value = "sendLogRecordNotification", nickname = "sendLogRecordNotification")
	@RequestMapping(value = "/AARService/sendLogRecordNotification/{id}", method = RequestMethod.POST )
	@ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "the id of the log record", required = true, dataType = "long", paramType = "path"),
        @ApiImplicitParam(name = "level", value = "level of the log record", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "clientId", value = "clientId of the log record", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "message", value = "level of the log record", required = true, dataType = "string", paramType = "body")
      })
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	@Produces({"application/json"})
	public ResponseEntity<Boolean> sendLogRecordNotification( 	@PathVariable Long id,
																@QueryParam("clientId") String clientId,
																@RequestBody String message) {
		log.info("--> sendLogRecordNotification: " + id);
		Boolean send = true;
		Log logRecord = new Log();
		logRecord.setDateTimeSent(new Date().getTime());
		logRecord.setId(clientId);
		logRecord.setLevel(Level.INFO);
		logRecord.setLog("This is the test log entry!");		
		
		WSRecordNotification notification = new WSRecordNotification(id, clientId, TopicConstants.LOGGING_TOPIC, new Date(), "Log", logRecord.getLog().toString(), "Info", logRecord.toString(), null);
		sendMessage(notification);
		
		log.info("sendLogRecordNotification -->");
	    return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendTopicinviteRecordNotification", nickname = "sendTopicinviteRecordNotification")
	@RequestMapping(value = "/AARService/sendTopicinviteRecordNotification/{id}", method = RequestMethod.POST )
	@ApiImplicitParams({
		@ApiImplicitParam(name = "id", value = "the id of the log record", required = true, dataType = "long", paramType = "path"),
        @ApiImplicitParam(name = "clientId", value = "the clientId to which the message belongs to", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "topicName", value = "the name of the topic", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "publishAllowed", value = "identifies if the client is allowed to connect as publisher", required = true, dataType = "boolean", paramType = "query"),
        @ApiImplicitParam(name = "subscribeAllowed", value = "identifies if the client is allowed to connect as subscriber", required = true, dataType = "boolean", paramType = "query")
      })
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	@Produces({"application/json"})
	public ResponseEntity<Boolean> sendTopicinviteRecordNotification( 	@PathVariable Long id,
																@QueryParam("clientId") String clientId,
																@QueryParam("topicName") String topicName,
																@QueryParam("publishAllowed") Boolean publishAllowed,
																@QueryParam("subscribeAllowed") Boolean subscribeAllowed) {
		log.info("--> sendTopicinviteRecordNotification");
		
		Boolean send = true;
		TopicInvite topicInvite = new TopicInvite();
		topicInvite.setId(clientId);
		topicInvite.setTopicName(topicName);
		topicInvite.setPublishAllowed(publishAllowed);
		topicInvite.setSubscribeAllowed(subscribeAllowed);
		
		WSRecordNotification notification = new WSRecordNotification(id, clientId, TopicConstants.TOPIC_INVITE_TOPIC, new Date(), "TopicInvite", "TopicInvite", "Info", topicInvite.toString(), null);
		sendMessage(notification);
		
		log.info("sendTopicinviteRecordNotification -->");
	    return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}
	
	private void sendMessage(Object object) {
		WSController.getInstance().sendMessage(mapper.objectToJSONString(object));
	}

}
