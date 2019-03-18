package eu.driver.aar.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.ws.rs.QueryParam;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.aar.service.dto.Record;
import eu.driver.aar.service.dto.Session;
import eu.driver.aar.service.dto.Szenario;
import eu.driver.aar.service.dto.Trial;
import eu.driver.aar.service.repository.RecordRepository;
import eu.driver.aar.service.repository.SessionRepository;
import eu.driver.aar.service.repository.SzenarioRepository;
import eu.driver.aar.service.repository.TrialRepository;
import eu.driver.aar.service.ws.WSController;
import eu.driver.aar.service.ws.mapper.StringJSONMapper;
import eu.driver.aar.service.ws.object.WSRecordNotification;
import eu.driver.adapter.core.CISAdapter;
import eu.driver.api.IAdaptorCallback;
import eu.driver.model.core.State;

@RestController
public class RecordRESTController implements IAdaptorCallback {
	
	private Logger log = Logger.getLogger(this.getClass());
	private StringJSONMapper mapper = new StringJSONMapper();
	
	private State currentTimingState = State.Idle;
	private Float currentTrialTimeSpeed = 0F;
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	@Autowired
	RecordRepository recordRepo;
	
	@Autowired
	TrialRepository trialRepo;
	
	@Autowired
	SzenarioRepository szenarioRepo;
	
	@Autowired
	SessionRepository sessionRepo;

	public RecordRESTController() {
		log.info("RecordRESTController");
	}
	
	@Override
	public void messageReceived(IndexedRecord key, IndexedRecord receivedMessage, String topicName) {
		Record record = new Record();
		record.setCreateDate(new Date());
		record.setRecordType(receivedMessage.getSchema().getName());
		eu.driver.model.edxl.EDXLDistribution msgKey = (eu.driver.model.edxl.EDXLDistribution) SpecificData.get().deepCopy(eu.driver.model.edxl.EDXLDistribution.SCHEMA$, key);
		record.setClientId(msgKey.getSenderID().toString());
		record.setTopic(topicName);
		
		record.setTrialDate(CISAdapter.getInstance().getTrialTime());
		
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
		} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("LargeDataUpdate")) {
			eu.driver.model.core.LargeDataUpdate msg = (eu.driver.model.core.LargeDataUpdate) SpecificData.get().deepCopy(eu.driver.model.core.LargeDataUpdate.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
			
			// Todo: Download the file and save it to the Disk, replace the link
		} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("GeoJSONEnvelope")) {
			eu.driver.model.geojson.GeoJSONEnvelope msg = (eu.driver.model.geojson.GeoJSONEnvelope) SpecificData.get().deepCopy(eu.driver.model.geojson.GeoJSONEnvelope.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("SessionMgmt")) {
			eu.driver.model.core.SessionMgmt msg = (eu.driver.model.core.SessionMgmt) SpecificData.get().deepCopy(eu.driver.model.core.SessionMgmt.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("PhaseMessage")) {
			eu.driver.model.core.PhaseMessage msg = (eu.driver.model.core.PhaseMessage) SpecificData.get().deepCopy(eu.driver.model.core.PhaseMessage.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("RolePlayer")) {
			eu.driver.model.core.RolePlayer msg = (eu.driver.model.core.RolePlayer) SpecificData.get().deepCopy(eu.driver.model.core.RolePlayer.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("Timing")) {
			eu.driver.model.core.Timing msg = (eu.driver.model.core.Timing) SpecificData.get().deepCopy(eu.driver.model.core.Timing.SCHEMA$, receivedMessage);
			// ToDo: handle start/stop/pause trial
			if (!msg.getState().equals(currentTimingState) || msg.getTrialTimeSpeed() != currentTrialTimeSpeed) {
				this.currentTimingState = msg.getState();
				this.currentTrialTimeSpeed = msg.getTrialTimeSpeed();
				record.setRecordJson(msg.toString());
			} else {
				// do nothing, no state update
				record = null;
			}
		} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("RequestChangeOfTrialStage")) {
			eu.driver.model.core.RequestChangeOfTrialStage msg = (eu.driver.model.core.RequestChangeOfTrialStage) SpecificData.get().deepCopy(eu.driver.model.core.RequestChangeOfTrialStage.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("ObserverToolAnswer")) {
			eu.driver.model.core.ObserverToolAnswer msg = (eu.driver.model.core.ObserverToolAnswer) SpecificData.get().deepCopy(eu.driver.model.core.ObserverToolAnswer.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else {
			// unknown data
			record = null;
			log.error("Unknown message received!");
		}
		
		if(record != null) {
			try {
				record = recordRepo.saveAndFlush(record);
				String data = record.getRecordJson();
				if (data.length() > 100) {
					data = data.substring(0, 100);
				}
				WSRecordNotification notification = new WSRecordNotification(record.getId(), 
						record.getClientId(),
						record.getTopic(),
						record.getCreateDate(), 
						record.getRecordType(), 
						record.getRecordJson(), 
						data);
				WSController.getInstance().sendMessage(mapper.objectToJSONString(notification));
			} catch(Exception e) {
				log.error("Error processing the message!", e);
			}
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
		/*if (records.size() > 20) {
			log.info("getAllRecords-->");
			return new ResponseEntity<List<Record>>(records.subList(records.size()-20, records.size()-1), HttpStatus.OK);
		} */
		
		log.info("getAllRecords-->");
		return new ResponseEntity<List<Record>>(records, HttpStatus.OK);
	}
	
	@ApiOperation(value = "getActualTrial", nickname = "getActualTrial")
	@RequestMapping(value = "/AARService/getActualTrial", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = ArrayList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ArrayList.class),
			@ApiResponse(code = 500, message = "Failure", response = ArrayList.class) })
	public ResponseEntity<Trial> getActualTrial() {
		log.info("-->getActualTrial");
		Trial trial = trialRepo.findActualTrial();
		
		log.info("getActualTrial-->");
		return new ResponseEntity<Trial>(trial, HttpStatus.OK);
	}
	
	@ApiOperation(value = "addNewTrial", nickname = "addNewTrial")
	@RequestMapping(value = "/AARService/addNewTrial", method = RequestMethod.POST)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "trialID", value = "the id of the trial", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "trialName", value = "the name of the trial", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "startDate", value = "the startDate of the trial", required = true, dataType = "date", paramType = "query"),
        @ApiImplicitParam(name = "endDate", value = "the endDate of the trial", required = true, dataType = "date", paramType = "query")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> addNewTrial(
			@QueryParam("trialID")String trialID, 
			@QueryParam("trialName")String trialName, 
			@QueryParam("startDate")String startDate, 
			@QueryParam("endDate")String endDate) {
		log.info("-->addNewTrial");
		
		Trial trial = trialRepo.findObjectByTrialId(trialID);
		if (trial == null) {
			trial = new Trial();
			trial.setTrialId(trialID);
			trial.setTrialName(trialName);
			try {
				trial.setStartDate(format.parse(startDate));
			} catch (Exception e) {
				
			}
		}
		try {
			trial.setEndDate(format.parse(endDate));
		} catch (Exception e) {
			
		}
		trial.setActual(true);
		trialRepo.saveAndFlush(trial);
		
		log.info("addNewTrial-->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
		
	}
	
	@ApiOperation(value = "addNewSzenario", nickname = "addNewSzenario")
	@RequestMapping(value = "/AARService/addNewSzenario", method = RequestMethod.POST)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "trialID", value = "the id of the trial", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "szenarioID", value = "the id of the szenario", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "szenarioName", value = "the name of the szenario", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "startDate", value = "the startDate of the szenario", required = true, dataType = "date", paramType = "query"),
        @ApiImplicitParam(name = "endDate", value = "the endDate of the szenario", required = true, dataType = "date", paramType = "query")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> addNewSzenario(
			@QueryParam("trialID")String trialID,
			@QueryParam("szenarioID")String szenarioID, 
			@QueryParam("szenarioName")String szenarioName, 
			@QueryParam("startDate")String startDate, 
			@QueryParam("endDate")String endDate) {
		log.info("-->addNewSzenario");
		
		Trial trial = trialRepo.findObjectByTrialId(trialID);
		if (trial == null) {
			return new ResponseEntity<Boolean>(false, HttpStatus.NOT_FOUND);
		} else {
			Szenario szenario = szenarioRepo.findObjectBySzenarioId(szenarioID);
			if (szenario != null) {
				return new ResponseEntity<Boolean>(false, HttpStatus.BAD_REQUEST);
			} else {
				szenario = new Szenario();
				szenario.setSzenarioId(szenarioID);
				szenario.setSzenarioName(szenarioName);
				try {
					szenario.setStartDate(format.parse(startDate));
					szenario.setEndDate(format.parse(endDate));
				} catch (Exception e) {
					
				}
				szenario.setTrial(trial);
				trial.addSzenario(szenario);
			}
			szenarioRepo.saveAndFlush(szenario);
		}
		
		log.info("addNewSzenario-->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
		
	}
	
	@ApiOperation(value = "addNewSession", nickname = "addNewSession")
	@RequestMapping(value = "/AARService/addNewSession", method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "szenarioID", value = "the id of the szenario", required = true, dataType = "string", paramType = "query"),
		@ApiImplicitParam(name = "sessionID", value = "the id of the trial", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "sessionName", value = "the name of the trial", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "startDate", value = "the startDate of the trial", required = true, dataType = "date", paramType = "query"),
        @ApiImplicitParam(name = "endDate", value = "the endDate of the trial", required = true, dataType = "date", paramType = "query")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> addNewSession(
			@QueryParam("szenarioID")String szenarioID, 
			@QueryParam("sessionID")String sessionID, 
			@QueryParam("sessionName")String sessionName, 
			@QueryParam("startDate")String startDate, 
			@QueryParam("endDate")String endDate) {
		log.info("-->addNewSession");
		
		Szenario szenario = szenarioRepo.findObjectBySzenarioId(szenarioID);
		if (szenario == null) {
			return new ResponseEntity<Boolean>(false, HttpStatus.NOT_FOUND);
		} else {
			Session session = sessionRepo.findObjectBySessionId(sessionID);
			if (session != null) {
				return new ResponseEntity<Boolean>(false, HttpStatus.BAD_REQUEST);
			} else {
				session = new Session();
				session.setSessionId(sessionID);
				session.setSessionName(sessionName);
				try {
					session.setStartDate(format.parse(startDate));
					session.setEndDate(format.parse(endDate));
				} catch (Exception e) {
					
				}
				session.setSzenario(szenario);
				szenario.addSession(session);
			}
			szenarioRepo.saveAndFlush(szenario);
		}
		
		log.info("addNewSession-->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
		
	}
	
	@ApiOperation(value = "getAllTimelineRecords", nickname = "getAllTimelineRecords")
	@RequestMapping(value = "/AARService/getAllTimelineRecords", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = ArrayList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ArrayList.class),
			@ApiResponse(code = 500, message = "Failure", response = ArrayList.class) })
	public ResponseEntity<List<Record>> getAllTimelineRecords() {
		log.info("-->getAllTimelineRecords");
		
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("AARSerivice");
		EntityManager em = factory.createEntityManager();
		String query = "SELECT NEW Record(i.id, i.topic, i.recordType) FROM Record";
		TypedQuery<Record> typedQuery = em.createQuery(query , Record.class);
		List<Record> records = typedQuery.getResultList();
		
		log.info("getAllTimelineRecords-->");
		return new ResponseEntity<List<Record>>(records, HttpStatus.OK);
	}
	

}
