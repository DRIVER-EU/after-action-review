package eu.driver.aar.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.QueryParam;

import net.sourceforge.plantuml.SourceStringReader;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.aar.service.dto.Record;
import eu.driver.aar.service.dto.RecordFilter;
import eu.driver.aar.service.dto.Session;
import eu.driver.aar.service.dto.Szenario;
import eu.driver.aar.service.dto.TopicReceiver;
import eu.driver.aar.service.dto.Trial;
import eu.driver.aar.service.repository.RecordRepository;
import eu.driver.aar.service.repository.SessionRepository;
import eu.driver.aar.service.repository.SzenarioRepository;
import eu.driver.aar.service.repository.TopicReceiverRepository;
import eu.driver.aar.service.repository.TrialRepository;
import eu.driver.aar.service.ws.WSController;
import eu.driver.aar.service.ws.mapper.StringJSONMapper;
import eu.driver.aar.service.ws.object.WSRecordNotification;
import eu.driver.adapter.core.CISAdapter;
import eu.driver.api.IAdaptorCallback;
import eu.driver.model.core.State;
import eu.driver.model.tm.SessionState;

@RestController
public class RecordRESTController implements IAdaptorCallback {

	private Logger log = Logger.getLogger(this.getClass());
	private StringJSONMapper mapper = new StringJSONMapper();

	private State currentTimingState = State.Idle;
	private Float currentTrialTimeSpeed = 0F;

	private SimpleDateFormat format = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");

	private RecordFilter actualFilter = new RecordFilter();

	@Autowired
	RecordRepository recordRepo;

	@Autowired
	TrialRepository trialRepo;

	@Autowired
	SzenarioRepository szenarioRepo;

	@Autowired
	SessionRepository sessionRepo;

	@Autowired
	TopicReceiverRepository topicReceiverRepo;

	@PersistenceContext(unitName = "AARService")
	private EntityManager entityManager;

	public RecordRESTController() {
		log.info("RecordRESTController");
	}

	@Override
	public void messageReceived(IndexedRecord key,
			IndexedRecord receivedMessage, String topicName) {
		Record record = new Record();
		record.setCreateDate(new Date());
		record.setTrialDate(CISAdapter.getInstance().getTrialTime());
		record.setRecordType(receivedMessage.getSchema().getName());
		eu.driver.model.edxl.EDXLDistribution msgKey = (eu.driver.model.edxl.EDXLDistribution) SpecificData
				.get().deepCopy(eu.driver.model.edxl.EDXLDistribution.SCHEMA$, key);
		record.setClientId(msgKey.getSenderID().toString());
		record.setTopic(topicName);

		record.setTrialDate(CISAdapter.getInstance().getTrialTime());

		if (receivedMessage.getSchema().getName().equalsIgnoreCase("Log")) {
			eu.driver.model.core.Log msg = (eu.driver.model.core.Log) SpecificData
					.get().deepCopy(eu.driver.model.core.Log.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName()
				.equalsIgnoreCase("TopicInvite")) {
			eu.driver.model.core.TopicInvite msg = (eu.driver.model.core.TopicInvite) SpecificData
					.get().deepCopy(eu.driver.model.core.TopicInvite.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());

			// create the TopicReceiver entries
			String clientId = msg.getId().toString();
			String receiverTopicName = msg.getTopicName().toString();
			Boolean subscribeAllowed = msg.getSubscribeAllowed();
			if (subscribeAllowed) {
				String trialId = "unknown";
				TopicReceiver topicReceiver = topicReceiverRepo
						.findObjectByTrialClientTopic(trialId, clientId, receiverTopicName);
				if (topicReceiver == null) {
					topicReceiver = new TopicReceiver();
					topicReceiver.setClientId(clientId);
					topicReceiver.setTopicName(receiverTopicName);
					topicReceiver.setTrialId(trialId);

					topicReceiverRepo.saveAndFlush(topicReceiver);
				}
			}
		} else if (receivedMessage.getSchema().getName()
				.equalsIgnoreCase("Alert")) {
			eu.driver.model.cap.Alert msg = (eu.driver.model.cap.Alert) SpecificData
					.get().deepCopy(eu.driver.model.cap.Alert.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName()
				.equalsIgnoreCase("SlRep")) {
			eu.driver.model.mlp.SlRep msg = (eu.driver.model.mlp.SlRep) SpecificData
					.get().deepCopy(eu.driver.model.mlp.SlRep.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName()
				.equalsIgnoreCase("FeatureCollection")) {
			eu.driver.model.geojson.FeatureCollection msg = (eu.driver.model.geojson.FeatureCollection) SpecificData
					.get().deepCopy(
							eu.driver.model.geojson.FeatureCollection.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName()
				.equalsIgnoreCase("TSO_2_0")) {
			eu.driver.model.emsi.TSO_2_0 msg = (eu.driver.model.emsi.TSO_2_0) SpecificData
					.get().deepCopy(eu.driver.model.emsi.TSO_2_0.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName()
				.equalsIgnoreCase("LargeDataUpdate")) {
			eu.driver.model.core.LargeDataUpdate msg = (eu.driver.model.core.LargeDataUpdate) SpecificData
					.get().deepCopy(
							eu.driver.model.core.LargeDataUpdate.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());

			// Todo: Download the file and save it to the Disk, replace the link
		} else if (receivedMessage.getSchema().getName()
				.equalsIgnoreCase("GeoJSONEnvelope")) {
			eu.driver.model.geojson.GeoJSONEnvelope msg = (eu.driver.model.geojson.GeoJSONEnvelope) SpecificData
					.get().deepCopy(
							eu.driver.model.geojson.GeoJSONEnvelope.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName()
				.equalsIgnoreCase("SessionMgmt")) {
			eu.driver.model.core.SessionMgmt msg = (eu.driver.model.core.SessionMgmt) SpecificData
					.get().deepCopy(eu.driver.model.core.SessionMgmt.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());

			// check the session
			String trialId = msg.getTrialId().toString();
			Trial trial = trialRepo.findObjectByTrialId(trialId);
			if (trial == null) {
				trial = new Trial();
				trial.setActual(true);
				trial.setTrialName(msg.getTrialName().toString());
				trial.setTrialId(msg.getTrialId().toString());
				trial.setStartDate(new Date());
			}
			if (msg.getSessionState()== SessionState.STOP) {
				trial.setEndDate(new Date());
			}

			String szenarioId = msg.getScenarioId().toString();
			Szenario szenario = szenarioRepo.findObjectBySzenarioId(szenarioId);
			if (szenario == null) {
				szenario = new Szenario();
				szenario.setTrial(trial);
				szenario.setSzenarioId(msg.getScenarioId().toString());
				szenario.setSzenarioName(msg.getScenarioName().toString());
				szenario.setStartDate(new Date());
				trial.addSzenario(szenario);
			}
			if (msg.getSessionState() == SessionState.STOP) {
				szenario.setEndDate(new Date());
			}

			String sessionId = msg.getSessionId().toString();
			Session session = sessionRepo.findObjectBySessionId(sessionId);
			if (session == null) {
				session = new Session();
				session.setSzenario(szenario);
				session.setSessionId(msg.getSessionId().toString());
				session.setSessionName(msg.getSessionName().toString());
				session.setStartDate(new Date());
			}
			if (msg.getSessionState() == SessionState.STOP) {
				session.setEndDate(new Date());
			}
			trialRepo.saveAndFlush(trial);
		} else if (receivedMessage.getSchema().getName()
				.equalsIgnoreCase("PhaseMessage")) {
			eu.driver.model.core.PhaseMessage msg = (eu.driver.model.core.PhaseMessage) SpecificData
					.get().deepCopy(eu.driver.model.core.PhaseMessage.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName()
				.equalsIgnoreCase("RolePlayer")) {
			eu.driver.model.core.RolePlayerMessage msg = (eu.driver.model.core.RolePlayerMessage) SpecificData
					.get().deepCopy(eu.driver.model.core.RolePlayerMessage.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName()
				.equalsIgnoreCase("Timing")) {
			eu.driver.model.core.Timing msg = (eu.driver.model.core.Timing) SpecificData
					.get().deepCopy(eu.driver.model.core.Timing.SCHEMA$, receivedMessage);
			if (!msg.getState().equals(currentTimingState)
					|| msg.getTrialTimeSpeed() != currentTrialTimeSpeed) {
				this.currentTimingState = msg.getState();
				this.currentTrialTimeSpeed = msg.getTrialTimeSpeed();
				record.setRecordJson(msg.toString());
			} else {
				// do nothing, no state update
				record = null;
			}
		} else if (receivedMessage.getSchema().getName()
				.equalsIgnoreCase("RequestChangeOfTrialStage")) {
			eu.driver.model.core.RequestChangeOfTrialStage msg = (eu.driver.model.core.RequestChangeOfTrialStage) SpecificData
					.get()
					.deepCopy(
							eu.driver.model.core.RequestChangeOfTrialStage.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else if (receivedMessage.getSchema().getName()
				.equalsIgnoreCase("ObserverToolAnswer")) {
			eu.driver.model.core.ObserverToolAnswer msg = (eu.driver.model.core.ObserverToolAnswer) SpecificData
					.get().deepCopy(
							eu.driver.model.core.ObserverToolAnswer.SCHEMA$, receivedMessage);
			record.setRecordJson(msg.toString());
		} else {
			// unknown data
			record = null;
			log.error("Unknown message received!");
		}

		if (record != null) {
			try {
				record = recordRepo.saveAndFlush(record);
				// check if the record needs to be send via the websocket
				boolean sendRecord = true;
				// ToDo: check if record meets filter criteras, if yes, push it to the client
				if (this.actualFilter != null && this.actualFilter.isFilterEnabled()) {
					// check if record meets filter criteria
					sendRecord = this.actualFilter.meetsRecordFilter(record);
				}
				
				if (sendRecord) {
					WSRecordNotification notification = new WSRecordNotification(
							record.getId(), record.getClientId(),
							record.getTopic(), record.getCreateDate(),
							record.getRecordType(), null, null);
					
					WSController.getInstance().sendMessage(
							mapper.objectToJSONString(notification));	
				}
			} catch (Exception e) {
				log.error("Error processing the message!", e);
			}
		}
	}

	@ApiOperation(value = "getAllRecords", nickname = "getAllRecords")
	@RequestMapping(value = "/AARService/getAllRecords", method = RequestMethod.GET)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "page", value = "the act. page of the client", required = false, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "size", value = "the act. size of the page records on the client", required = false, dataType = "int", paramType = "query") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = ArrayList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ArrayList.class),
			@ApiResponse(code = 500, message = "Failure", response = ArrayList.class) })
	public ResponseEntity<List<Record>> getAllRecords(
			@RequestParam(value="page", required=false) Integer page,
			@RequestParam(value="size", required=false) Integer size) {
		log.info("-->getAllRecords");
		String query = "SELECT NEW Record(i.id, i.clientId, i.sessionId, i.topic, i.recordType, i.createDate, i.trialDate) FROM Record i";

		// create the query using the active Filter
		if (this.actualFilter == null) {
			query += this.createFilterQuery();
		}

		TypedQuery<Record> typedQuery = entityManager.createQuery(query, Record.class);
		// use the filtering here
		if (page != null && size != null) {
			typedQuery.setFirstResult(page * size);
			typedQuery.setMaxResults(size);
		}
		List<Record> records = typedQuery.getResultList();

		log.info("getAllRecords-->");
		return new ResponseEntity<List<Record>>(records, HttpStatus.OK);
	}

	@ApiOperation(value = "getRecord", nickname = "getRecord")
	@RequestMapping(value = "/AARService/getRecord/{id}", method = RequestMethod.GET)
	@ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "the id of the log record", required = true, dataType = "long", paramType = "path") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = ArrayList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ArrayList.class),
			@ApiResponse(code = 500, message = "Failure", response = ArrayList.class) })
	public ResponseEntity<Record> getRecord(@PathVariable Long id) {
		log.info("-->getAllRecords");
		Record record = recordRepo.findObjectById(id);

		log.info("getAllRecords-->");
		return new ResponseEntity<Record>(record, HttpStatus.OK);
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
			@ApiImplicitParam(name = "endDate", value = "the endDate of the trial", required = true, dataType = "date", paramType = "query") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> addNewTrial(
			@QueryParam("trialID") String trialID,
			@QueryParam("trialName") String trialName,
			@QueryParam("startDate") String startDate,
			@QueryParam("endDate") String endDate) {
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
			@ApiImplicitParam(name = "endDate", value = "the endDate of the szenario", required = true, dataType = "date", paramType = "query") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> addNewSzenario(
			@QueryParam("trialID") String trialID,
			@QueryParam("szenarioID") String szenarioID,
			@QueryParam("szenarioName") String szenarioName,
			@QueryParam("startDate") String startDate,
			@QueryParam("endDate") String endDate) {
		log.info("-->addNewSzenario");

		Trial trial = trialRepo.findObjectByTrialId(trialID);
		if (trial == null) {
			return new ResponseEntity<Boolean>(false, HttpStatus.NOT_FOUND);
		} else {
			Szenario szenario = szenarioRepo.findObjectBySzenarioId(szenarioID);
			if (szenario != null) {
				return new ResponseEntity<Boolean>(false,
						HttpStatus.BAD_REQUEST);
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
			@ApiImplicitParam(name = "sessionID", value = "the id of the session", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "sessionName", value = "the name of the session", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "startDate", value = "the startDate of the session", required = true, dataType = "date", paramType = "query"),
			@ApiImplicitParam(name = "endDate", value = "the endDate of the session", required = true, dataType = "date", paramType = "query") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> addNewSession(
			@QueryParam("szenarioID") String szenarioID,
			@QueryParam("sessionID") String sessionID,
			@QueryParam("sessionName") String sessionName,
			@QueryParam("startDate") String startDate,
			@QueryParam("endDate") String endDate) {
		log.info("-->addNewSession");

		Szenario szenario = szenarioRepo.findObjectBySzenarioId(szenarioID);
		if (szenario == null) {
			return new ResponseEntity<Boolean>(false, HttpStatus.NOT_FOUND);
		} else {
			Session session = sessionRepo.findObjectBySessionId(sessionID);
			if (session != null) {
				return new ResponseEntity<Boolean>(false,
						HttpStatus.BAD_REQUEST);
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

		String query = "SELECT NEW Record(i.id, i.topic, i.recordType, i.createDate) FROM Record i";
		if (this.actualFilter == null) {
			query += this.createFilterQuery();
		}

		TypedQuery<Record> typedQuery = entityManager.createQuery(query, Record.class);
		List<Record> records = typedQuery.getResultList();

		log.info("getAllTimelineRecords-->");
		return new ResponseEntity<List<Record>>(records, HttpStatus.OK);
	}

	@ApiOperation(value = "getActualFilter", nickname = "getActualFilter")
	@RequestMapping(value = "/AARService/getActualFilter", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = RecordFilter.class),
			@ApiResponse(code = 400, message = "Bad Request", response = RecordFilter.class),
			@ApiResponse(code = 500, message = "Failure", response = RecordFilter.class) })
	public ResponseEntity<RecordFilter> getActualFilter() {
		log.info("-->getActualFilter");

		log.info("getActualFilter-->");
		return new ResponseEntity<RecordFilter>(this.actualFilter,
				HttpStatus.OK);
	}

	@ApiOperation(value = "setActualFilter", nickname = "setActualFilter")
	@RequestMapping(value = "/AARService/setActualFilter", method = RequestMethod.POST)
	@ApiImplicitParams({ @ApiImplicitParam(name = "filter", value = "the filter that should be applied", required = true, dataType = "json", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = RecordFilter.class),
			@ApiResponse(code = 400, message = "Bad Request", response = RecordFilter.class),
			@ApiResponse(code = 500, message = "Failure", response = RecordFilter.class) })
	public ResponseEntity<RecordFilter> setActualFilter(
			@RequestBody RecordFilter filter) {
		log.info("-->setActualFilter");

		this.actualFilter = filter;

		log.info("setActualFilter-->");
		return new ResponseEntity<RecordFilter>(this.actualFilter,
				HttpStatus.OK);
	}

	@ApiOperation(value = "getRecordTypes", nickname = "getRecordTypes")
	@RequestMapping(value = "/AARService/getRecordTypes", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = ArrayList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ArrayList.class),
			@ApiResponse(code = 500, message = "Failure", response = ArrayList.class) })
	public ResponseEntity<List<String>> getRecordTypes() {
		log.info("-->getRecordTypes");
		
		String query = "SELECT DISTINCT (i.recordType) FROM Record i";
		
		TypedQuery<String> typedQuery = entityManager.createQuery(query, String.class);
		List<String> records = typedQuery.getResultList();

		log.info("getRecordTypes-->");
		return new ResponseEntity<List<String>>(records, HttpStatus.OK);
	}

	@ApiOperation(value = "getTopicNames", nickname = "getTopicNames")
	@RequestMapping(value = "/AARService/getTopicNames", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = ArrayList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ArrayList.class),
			@ApiResponse(code = 500, message = "Failure", response = ArrayList.class) })
	public ResponseEntity<List<String>> getTopicNames() {
		log.info("-->getTopicNames");
		
		String query = "SELECT DISTINCT (i.topic) FROM Record i";
		
		TypedQuery<String> typedQuery = entityManager.createQuery(query, String.class);
		List<String> records = typedQuery.getResultList();

		log.info("getTopicNames-->");
		return new ResponseEntity<List<String>>(records, HttpStatus.OK);
	}

	@ApiOperation(value = "getSenderClientIds", nickname = "getSenderClientIds")
	@RequestMapping(value = "/AARService/getSenderClientIds", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = ArrayList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ArrayList.class),
			@ApiResponse(code = 500, message = "Failure", response = ArrayList.class) })
	public ResponseEntity<List<String>> getSenderClientIds() {
		log.info("-->getSenderClientIds");
		
		String query = "SELECT DISTINCT (i.clientId) FROM Record i";
		
		TypedQuery<String> typedQuery = entityManager.createQuery(query, String.class);
		List<String> records = typedQuery.getResultList();

		log.info("getSenderClientIds-->");
		return new ResponseEntity<List<String>>(records, HttpStatus.OK);
	}

	@ApiOperation(value = "getReceiverClientIds", nickname = "getReceiverClientIds")
	@RequestMapping(value = "/AARService/getReceiverClientIds", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = ArrayList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ArrayList.class),
			@ApiResponse(code = 500, message = "Failure", response = ArrayList.class) })
	public ResponseEntity<List<String>> getReceiverClientIds() {
		log.info("-->getReceiverClientIds");

		log.info("getReceiverClientIds-->");
		return new ResponseEntity<List<String>>(new ArrayList<String>(),
				HttpStatus.OK);
	}

	@ApiOperation(value = "createSequenceDiagram", nickname = "createSequenceDiagram")
	@RequestMapping(value = "/AARService/createSequenceDiagram", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = byte[].class),
			@ApiResponse(code = 400, message = "Bad Request", response = byte[].class),
			@ApiResponse(code = 500, message = "Failure", response = byte[].class) })
	public ResponseEntity<byte[]> createSequenceDiagram() {
		log.info("-->createSequenceDiagram");
		ByteArrayOutputStream bous = new ByteArrayOutputStream();
		List<Record> records = getAllTimelineRecords().getBody();
		if (records != null && records.size() > 0) {
			List<TopicReceiver> receivers = topicReceiverRepo.findAll();
			String source = createSequenceDiagramString(records, receivers);
			SourceStringReader reader = new SourceStringReader(source);
		    // Write the first image to "png"
			String desc = null;
		    try {
				desc = reader.generateImage(bous);
			} catch (IOException e) {
				log.error("Error creating the sequence diagram!");
			}
		    // Return a null string if no generation
		    byte[] media = bous.toByteArray();
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_PNG);
		    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
		    log.info("createSequenceDiagram-->");
		    return new ResponseEntity<byte[]>(media, headers, HttpStatus.OK);
		}
		log.info("createSequenceDiagram-->");
		return new ResponseEntity<byte[]>("".getBytes(), HttpStatus.OK);
	}

	public ResponseEntity<Boolean> exportData() {
		log.info("-->exportData");

		log.info("exportData-->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	private String createFilterQuery() {
		String query = "";
		Boolean firstParam = true;

		if (this.actualFilter.getId() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			}
			query += "i.id=" + this.actualFilter.getId();
		}

		if (this.actualFilter.getRecordType() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			} else {
				query += " AND ";
			}
			query += "i.recordType=" + this.actualFilter.getRecordType();
		}

		if (this.actualFilter.getTopicName() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			} else {
				query += " AND ";
			}
			query += "i.topicName=" + this.actualFilter.getTopicName();
		}

		if (this.actualFilter.getSenderClientId() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			} else {
				query += " AND ";
			}
			query += "i.clientId=" + this.actualFilter.getSenderClientId();
		}

		/*
		 * if (this.actualFilter.getReceiverClientId() != null) {
		 * 
		 * }
		 */

		if (this.actualFilter.getFromDate() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			} else {
				query += " AND ";
			}
			query += "i.createDate>=" + this.actualFilter.getFromDate();
		}

		if (this.actualFilter.getToDate() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			} else {
				query += " AND ";
			}
			query += "i.createDate<=" + this.actualFilter.getToDate();
		}

		return query;

	}
	
	private String createSequenceDiagramString(List<Record> records, List<TopicReceiver> receivers) {
		log.info("-->createSequenceDiagramString");
		String data = "@startuml\n";
		data += "Bob -> Alice : hello\n";
		data += "@enduml\n";

		log.info("createSequenceDiagramString-->");
		return data;
	}

}
