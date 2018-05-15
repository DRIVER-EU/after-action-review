package eu.driver.aar.service.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.aar.service.dto.record.Record;
import eu.driver.aar.service.repository.RecordRepository;
import eu.driver.aar.service.ws.WSController;
import eu.driver.aar.service.ws.mapper.StringJSONMapper;
import eu.driver.aar.service.ws.object.WSRecordNotification;
import eu.driver.api.IAdaptorCallback;
import eu.driver.model.core.Command;

@RestController
public class RecordRESTController implements IAdaptorCallback {
	
	private Logger log = Logger.getLogger(this.getClass());
	private StringJSONMapper mapper = new StringJSONMapper();
	
	@Autowired
	RecordRepository recordRepo;

	public RecordRESTController() {
		log.info("RecordRESTController");
	}
	
	@Override
	public void messageReceived(IndexedRecord key, IndexedRecord receivedMessage) {
		Record record = new Record();
		record.setCreateDate(new Date());
		record.setRecordType(receivedMessage.getSchema().getName());
		eu.driver.model.edxl.EDXLDistribution msgKey = (eu.driver.model.edxl.EDXLDistribution) SpecificData.get().deepCopy(eu.driver.model.edxl.EDXLDistribution.SCHEMA$, key);
		record.setClientId(msgKey.getSenderID().toString());
		
		if (receivedMessage.getSchema().getName().equalsIgnoreCase("Log")) {
			eu.driver.model.core.Log msg = (eu.driver.model.core.Log) SpecificData.get().deepCopy(eu.driver.model.core.Log.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("TopicInvite")) {
			eu.driver.model.core.TopicInvite msg = (eu.driver.model.core.TopicInvite) SpecificData.get().deepCopy(eu.driver.model.core.TopicInvite.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("Alert")) {
			eu.driver.model.cap.Alert msg = (eu.driver.model.cap.Alert) SpecificData.get().deepCopy(eu.driver.model.cap.Alert.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("SlRep")) {
			eu.driver.model.mlp.SlRep msg = (eu.driver.model.mlp.SlRep) SpecificData.get().deepCopy(eu.driver.model.mlp.SlRep.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("FeatureCollection")) {
			eu.driver.model.geojson.FeatureCollection msg = (eu.driver.model.geojson.FeatureCollection) SpecificData.get().deepCopy(eu.driver.model.geojson.FeatureCollection.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("TSO_2_0")) {
			eu.driver.model.emsi.TSO_2_0 msg = (eu.driver.model.emsi.TSO_2_0) SpecificData.get().deepCopy(eu.driver.model.emsi.TSO_2_0.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("TimingControl")) {
			eu.driver.model.core.TimingControl msg = (eu.driver.model.core.TimingControl) SpecificData.get().deepCopy(eu.driver.model.core.TimingControl.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
			
			// ToDo: handle start/stop/pause trial
			if (msg.getCommand().equals(Command.Stop)) {
				// stop command received, trial is done, collect all trial specific data
				
			}
		} else {
			// unknown data
			record = null;
			log.error("Unknown message received!");
		}
		
		if(record != null) {
			record = recordRepo.saveAndFlush(record);
			WSRecordNotification notification = new WSRecordNotification(record.getId(), record.getClientId(), record.getCreateDate(), record.getRecordType(), record.getRecordJson(), record.getRecordData());
			WSController.getInstance().sendMessage(mapper.objectToJSONString(notification));	
		}
	}
	
	@ApiOperation(value = "getAllRecords", nickname = "getAllRecords")
	@RequestMapping(value = "/AARService/getAllRecords", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = ArrayList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ArrayList.class),
			@ApiResponse(code = 500, message = "Failure", response = ArrayList.class) })
	public ResponseEntity<List<Record>> getAllRecords() {
		log.info("-->getAllRecords");
		List<Record> records = recordRepo.findAll();
		
		log.info("getAllRecords-->");
		return new ResponseEntity<List<Record>>(records, HttpStatus.OK);
	}
	

}
