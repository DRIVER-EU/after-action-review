package eu.driver.aar.service.controller;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
import eu.driver.model.core.DataType;
import eu.driver.model.core.LargeDataUpdate;
import eu.driver.model.core.Level;
import eu.driver.model.core.Log;
import eu.driver.model.core.TopicInvite;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class WSNotificationController {

   private final Logger log = Logger.getLogger(this.getClass());

   private final StringJSONMapper mapper = new StringJSONMapper();

   @Bean
   public WebSocketHandler wsHandler() {
      return new WebSocketServer();
   }

   int index = 10001;

   public WSNotificationController() {
      Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
         @Override
         public void run() {
            try {
               final int id = index++;
               final String clientId = "client1";
               sendMessage(createLargeDataUpdateNotification(id, clientId));
            } catch (final Exception ex) {
               ex.printStackTrace();
            }
         }

         private Object createLogNotification(final int id, final String clientId) {
            final Level level = Level.values()[id % Level.values().length];
            final Log record = new Log();
            record.setDateTimeSent(new Date().getTime());
            record.setId(clientId);
            record.setLevel(level);
            record.setLog("This is the test log entry!");
            final WSRecordNotification notification = new WSRecordNotification((long) id, clientId, TopicConstants.LOGGING_TOPIC, new Date(), "Log", record.toString(), null);
            System.err.println("WSNotificationController.WSNotificationController().new Callable() {...}.call() Sending message: " + id + " " + notification);
            return notification;
         }

         private Object createLargeDataUpdateNotification(final int id, final String clientId) {
            final LargeDataUpdate record = new LargeDataUpdate();
            record.setDataType(DataType.excel);
            record.setTitle("LDU");
            record.setDescription("This is the test LDU entry!");
            final WSRecordNotification notification = new WSRecordNotification((long) id, clientId, TopicConstants.LARGE_DATA_UPDTAE, new Date(), "LargeDataUpdate", record.toString(), null);
            System.err.println("WSNotificationController.WSNotificationController().new Callable() {...}.call() Sending message: " + id + " " + notification);
            return notification;
         }
      }, 0, 10, TimeUnit.SECONDS);
      /*
      */
   }

   @ApiOperation(value = "sendLogRecordNotification", nickname = "sendLogRecordNotification")
   @RequestMapping(value = "/AARService/sendLogRecordNotification/{id}", method = RequestMethod.POST)
   @ApiImplicitParams({
         @ApiImplicitParam(name = "id", value = "the id of the log record", required = true, dataType = "long", paramType = "path"),
         @ApiImplicitParam(name = "level", value = "level of the log record", required = true, dataType = "string", paramType = "query"),
         @ApiImplicitParam(name = "clientId", value = "clientId of the log record", required = true, dataType = "string", paramType = "query"),
         @ApiImplicitParam(name = "message", value = "level of the log record", required = true, dataType = "string", paramType = "body")
   })
   @ApiResponses(value = {
         @ApiResponse(code = 200, message = "Success", response = Boolean.class),
         @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
         @ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
   @Produces({ "application/json" })
   public ResponseEntity<Boolean> sendLogRecordNotification(@PathVariable final Long id,
         @QueryParam("clientId") final String clientId,
         @RequestBody final String message) {
      log.info("--> sendLogRecordNotification: " + id);
      final Boolean send = true;
      final Log logRecord = new Log();
      logRecord.setDateTimeSent(new Date().getTime());
      logRecord.setId(clientId);
      logRecord.setLevel(Level.INFO);
      logRecord.setLog("This is the test log entry!");

      final WSRecordNotification notification = new WSRecordNotification(id, clientId, TopicConstants.LOGGING_TOPIC, new Date(), "Log", logRecord.toString(), null);
      sendMessage(notification);

      log.info("sendLogRecordNotification -->");
      return new ResponseEntity<Boolean>(send, HttpStatus.OK);
   }

   @ApiOperation(value = "sendTopicinviteRecordNotification", nickname = "sendTopicinviteRecordNotification")
   @RequestMapping(value = "/AARService/sendTopicinviteRecordNotification/{id}", method = RequestMethod.POST)
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
         @ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
   @Produces({ "application/json" })
   public ResponseEntity<Boolean> sendTopicinviteRecordNotification(@PathVariable final Long id,
         @QueryParam("clientId") final String clientId,
         @QueryParam("topicName") final String topicName,
         @QueryParam("publishAllowed") final Boolean publishAllowed,
         @QueryParam("subscribeAllowed") final Boolean subscribeAllowed) {
      log.info("--> sendTopicinviteRecordNotification");

      final Boolean send = true;
      final TopicInvite topicInvite = new TopicInvite();
      topicInvite.setId(clientId);
      topicInvite.setTopicName(topicName);
      topicInvite.setPublishAllowed(publishAllowed);
      topicInvite.setSubscribeAllowed(subscribeAllowed);

      final WSRecordNotification notification = new WSRecordNotification(id, clientId, TopicConstants.TOPIC_INVITE_TOPIC, new Date(), "TopicInvite", topicInvite.toString(), null);
      sendMessage(notification);

      log.info("sendTopicinviteRecordNotification -->");
      return new ResponseEntity<Boolean>(send, HttpStatus.OK);
   }

   private void sendMessage(final Object object) {
      WSController.getInstance().sendMessage(mapper.objectToJSONString(object));
   }

}
