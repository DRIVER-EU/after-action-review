package eu.driver.aar.service.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketHandler;

import eu.driver.aar.service.constants.AARConstants;
import eu.driver.aar.service.ws.WSController;
import eu.driver.aar.service.ws.WebSocketServer;
import eu.driver.aar.service.ws.mapper.StringJSONMapper;
import eu.driver.aar.service.ws.object.WSRecordNotification;
import eu.driver.adapter.constants.TopicConstants;
import eu.driver.model.core.Level;
import eu.driver.model.core.Log;
import eu.driver.model.core.TopicInvite;
import eu.driver.model.edxl.DistributionKind;
import eu.driver.model.edxl.DistributionStatus;
import eu.driver.model.edxl.EDXLDistribution;
import eu.driver.model.sim.config.SessionManagement;
import eu.driver.model.sim.config.SessionState;

@RestController
public class WSNotificationController {
	
	private Logger log = Logger.getLogger(this.getClass());
	private StringJSONMapper mapper = new StringJSONMapper();
	
	@Autowired
	private RecordRESTController recRestController;
	
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
		
		LocalDateTime localDate = LocalDateTime.now();
		Date sendDate = Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
		
		WSRecordNotification notification = new WSRecordNotification(id, clientId, TopicConstants.LOGGING_TOPIC, sendDate, "Log", logRecord.getLog().toString(), AARConstants.RECORD_MSG_TYPE_INFO, AARConstants.RECORD_RUN_TYPE_IN, logRecord.toString(), null);
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
		
		LocalDateTime localDate = LocalDateTime.now();
		Date sendDate = Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
		//instant ins = Instant.now();
		WSRecordNotification notification = new WSRecordNotification(id, clientId, TopicConstants.TOPIC_INVITE_TOPIC, sendDate, "TopicInvite", "TopicInvite", AARConstants.RECORD_MSG_TYPE_INFO, AARConstants.RECORD_RUN_TYPE_IN, topicInvite.toString(), null);
		sendMessage(notification);
		
		log.info("sendTopicinviteRecordNotification -->");
	    return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendSessionMgmtMessage", nickname = "sendSessionMgmtMessage")
	@RequestMapping(value = "/AARService/sendSessionMgmtMessage", method = RequestMethod.POST )
	@ApiImplicitParams({
		@ApiImplicitParam(name = "trialId", value = "the id of the trial", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "scenarioId", value = "the id of the scenario", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "sessionId", value = "the id of the session", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "name", value = "the name of the session", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "sessionState", value = "the state of the session", required = true, dataType = "string", paramType = "query", allowableValues="Initializing,Started,Stopped,Closed")
      })
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	@Produces({"application/json"})
	public ResponseEntity<Boolean> sendSessionMgmtMessage(	@QueryParam("trialId") String trialId,
															@QueryParam("scenarioId") String scenarioId,
															@QueryParam("sessionId") String sessionId,
															@QueryParam("name") String name,
															@QueryParam("sessionState") String sessionState) {
		log.info("--> sendSessionMgmtMessage");
		
		Boolean send = true;
		SessionManagement sessionMgmt = new SessionManagement();
		sessionMgmt.setId(sessionId);
		sessionMgmt.setName(name);
		sessionMgmt.setState(SessionState.valueOf(sessionState));
		sessionMgmt.getTags().put("trialid", trialId);
		sessionMgmt.getTags().put("trialName","TestTrial");
		sessionMgmt.getTags().put("scenarioId",scenarioId);
		sessionMgmt.getTags().put("scenarioName","TestScenario");
		
		
		eu.driver.model.edxl.EDXLDistribution msgKey = new EDXLDistribution("test_01", "Swagger", new Date().getTime(), (new Date().getTime())+3600000, DistributionStatus.Actual, DistributionKind.Report);
		recRestController.messageReceived(msgKey, sessionMgmt, TopicConstants.SESSION_MGMT_TOPIC);
		
		log.info("sendSessionMgmtMessage -->");
	    return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}
	
	
	private void sendMessage(Object object) {
		WSController.getInstance().sendMessage(mapper.objectToJSONString(object));
	}

}
