package eu.driver.aar.service.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
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
import eu.driver.api.IAdaptorCallback;

@RestController
public class RecordRESTController implements IAdaptorCallback {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	RecordRepository recordRepo;

	public RecordRESTController() {
		log.info("RecordRESTController");
	}
	
	@Override
	public void messageReceived(IndexedRecord receivedMessage) {
		log.info("log message received!");
		if (receivedMessage.getSchema().getName().equalsIgnoreCase("Log")) {
			eu.driver.model.core.Log logMsg = (eu.driver.model.core.Log) SpecificData.get().deepCopy(eu.driver.model.core.Log.SCHEMA$, receivedMessage); 
				
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
