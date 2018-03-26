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
import eu.driver.aar.service.ws.object.WSLogNotification;

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
	
	@ApiOperation(value = "sendRecordNotification", nickname = "sendRecordNotification")
	@RequestMapping(value = "/AARService/sendRecordNotification/{id}", method = RequestMethod.POST )
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
	public ResponseEntity<Boolean> sendRecordNotification( 	@PathVariable Long id,
															@QueryParam("level") String level,
															@QueryParam("clientId") String clientId,
															@RequestBody String message) {
		log.info("--> sendRecordNotification: " + id);
		Boolean send = true;
		
		WSLogNotification notification = new WSLogNotification(id, level, clientId, new Date(), message);
		WSController.getInstance().sendMessage(mapper.objectToJSONString(notification));
		
		log.info("sendRecordNotification -->");
	    return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}

}
