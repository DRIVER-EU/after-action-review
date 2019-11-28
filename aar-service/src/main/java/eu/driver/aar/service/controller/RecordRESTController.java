package eu.driver.aar.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.ws.rs.QueryParam;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import eu.driver.aar.service.constants.AARConstants;
import eu.driver.aar.service.dto.Attachment;
import eu.driver.aar.service.dto.Record;
import eu.driver.aar.service.dto.RecordFilter;
import eu.driver.aar.service.dto.Session;
import eu.driver.aar.service.dto.Szenario;
import eu.driver.aar.service.dto.TopicReceiver;
import eu.driver.aar.service.dto.Trial;
import eu.driver.aar.service.repository.AttachmentRepository;
import eu.driver.aar.service.repository.RecordRepository;
import eu.driver.aar.service.repository.SessionRepository;
import eu.driver.aar.service.repository.SzenarioRepository;
import eu.driver.aar.service.repository.TopicReceiverRepository;
import eu.driver.aar.service.repository.TrialRepository;
import eu.driver.aar.service.utils.FileStorageService;
import eu.driver.aar.service.ws.WSController;
import eu.driver.aar.service.ws.mapper.StringJSONMapper;
import eu.driver.aar.service.ws.object.WSRecordNotification;
import eu.driver.adapter.constants.TopicConstants;
import eu.driver.adapter.core.CISAdapter;
import eu.driver.adapter.properties.ClientProperties;
import eu.driver.api.IAdaptorCallback;
import eu.driver.model.cap.Info;
import eu.driver.model.cap.MsgType;
import eu.driver.model.core.Level;
import eu.driver.model.core.ObserverToolAnswer;
import eu.driver.model.core.Question;
import eu.driver.model.core.State;
import eu.driver.model.core.TypeOfQuestion;
import eu.driver.model.geojson.photo.Feature;
import eu.driver.model.geojson.photo.properties;
import eu.driver.model.geojson.photo.files.files;
import eu.driver.model.tm.SessionState;

@RestController
public class RecordRESTController implements IAdaptorCallback {

	private Logger log = Logger.getLogger(this.getClass());
	private StringJSONMapper mapper = new StringJSONMapper();

	private State currentTimingState = State.Idle;
	private Float currentTrialTimeSpeed = 0F;

	private SimpleDateFormat format = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	private Double currentPageSize = 20D;
	
	private Map<String, Boolean> registeredCallbacks = new HashMap<String, Boolean>();
	private String ownClientID = ClientProperties.getInstance().getProperty("client.id");
	
	private Map<String, RecordFilter> sessionFilters = new HashMap<String, RecordFilter>();
	
	private Boolean connectTB = Boolean.parseBoolean(ClientProperties.getInstance().getProperty("connect.testbed", "true"));

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
	
	@Autowired
	AttachmentRepository attachmentRecord;

	@PersistenceContext(unitName = "AARService")
	private EntityManager entityManager;
	
	@Autowired
    private FileStorageService fileStorageService;
	
	private final String ANSWER_OBS_IN = "_OBSERVER_IL";
	private final String ANSWER_OBS_BL = "_OBSERVER_BL";
	
	private final String ANSWER_PRACT_FIE = "_FIE";
	private final String ANSWER_ALL_TRIAL = "_PARTICIPANT";
	
	private final String ANSWER_PRACT_SOLUTION = "_PRACT_S";
		
	private final String POST_BL_EXT = " BL";

	public RecordRESTController() {
		log.info("RecordRESTController");
	}

	@Override
	public synchronized void messageReceived(IndexedRecord key,
			IndexedRecord receivedMessage, String topicName) {
		Record record = new Record();
		try {
			record.setCreateDate(new Date());
			if (connectTB) {
				record.setTrialDate(CISAdapter.getInstance().getTrialTime());
			} else {
				record.setTrialDate(new Date());
			}
			record.setRecordType(receivedMessage.getSchema().getName());
			record.setRunType(AARConstants.RECORD_RUN_TYPE_IN);
			eu.driver.model.edxl.EDXLDistribution msgKey = (eu.driver.model.edxl.EDXLDistribution) SpecificData
					.get().deepCopy(eu.driver.model.edxl.EDXLDistribution.SCHEMA$, key);
			
			record.setClientId(msgKey.getSenderID().toString());
			record.setTopic(topicName);

		
			if (receivedMessage.getSchema().getName().equalsIgnoreCase("Log")) {
				eu.driver.model.core.Log msg = (eu.driver.model.core.Log) SpecificData
						.get().deepCopy(eu.driver.model.core.Log.SCHEMA$, receivedMessage);
				
				record.setRecordJson(msg.toString());
				String logMsg = msg.getLog().toString();
				if (logMsg.length() > 100) {
					record.setHeadline(logMsg.substring(0, 97) + "...");
				} else {
					record.setHeadline(msg.getLog().toString());
				}
				
				if (topicName.equalsIgnoreCase(TopicConstants.LOGGING_TOPIC)) {
					if (msg.getLevel().equals(Level.ERROR) || msg.getLevel().equals(Level.CRITICAL)) {
						record.setMsgType(AARConstants.RECORD_MSG_TYPE_ERROR);
					} else if (msg.getLevel().equals(Level.WARN)) {
						record.setMsgType(AARConstants.RECORD_MSG_TYPE_WARN);
					} else {
						record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
					}
				} else {
					record.setMsgType(AARConstants.RECORD_MSG_TYPE_EVAL);
				}
				
				
			} else if (receivedMessage.getSchema().getName()
					.equalsIgnoreCase("TopicInvite")) {
				eu.driver.model.core.TopicInvite msg = (eu.driver.model.core.TopicInvite) SpecificData
						.get().deepCopy(eu.driver.model.core.TopicInvite.SCHEMA$, receivedMessage);
				record.setRecordJson(msg.toString());
	
				// create the TopicReceiver entries
				String clientId = msg.getId().toString();
				String receiverTopicName = msg.getTopicName().toString();
				Boolean subscribeAllowed = msg.getSubscribeAllowed();
				
				String headline = "TopicInvite: " + clientId + " for " + receiverTopicName;
				if (msg.getSubscribeAllowed() && msg.getPublishAllowed()) {
					headline += " to publish/subscribe";
				} else if (msg.getSubscribeAllowed()) {
					headline += " to subscribe";
				} else if (msg.getPublishAllowed()) {
					headline += " to publish";
				}
				record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
				record.setHeadline(headline);
				if (ownClientID.equalsIgnoreCase(clientId)) {
					// add a callback if not done already
					if (registeredCallbacks.get(receiverTopicName) == null) {
						log.info("Adding a callback receiver for: " + receiverTopicName);
						CISAdapter.getInstance().addCallback(this, receiverTopicName);
						registeredCallbacks.put(receiverTopicName, true);
					}
				} else if (subscribeAllowed && !ownClientID.equalsIgnoreCase(clientId + "-Backup")) {
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
				if (msg.getMsgType().equals(MsgType.Ack )) {
					record.setMsgType(AARConstants.RECORD_MSG_TYPE_ACK);
					record.setHeadline(msg.getSender().toString() + " + " + msg.getReferences().toString());
				} else if (msg.getMsgType().equals(MsgType.Error)) {
					record.setMsgType(AARConstants.RECORD_MSG_TYPE_ERROR);
					record.setHeadline(msg.getSender().toString() + " + " + msg.getNote().toString());
				} else if (msg.getMsgType().equals(MsgType.Cancel)) {
					record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
					record.setHeadline("Cancel message received.");
				} else {
					record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
					if (msg.getInfo() instanceof Info) {
						record.setHeadline(((Info)msg.getInfo()).getHeadline().toString());
					} else {
						List<Info> infos = (List<Info>)msg.getInfo();
						if (infos != null) {
							if (infos.get(0).getHeadline() != null) {
								record.setHeadline(infos.get(0).getHeadline().toString());	
							} else {
								record.setHeadline("New CAP was send from: " + record.getClientId());
							}
						}
					}
				}
			} else if (receivedMessage.getSchema().getName()
					.equalsIgnoreCase("SlRep")) {
				eu.driver.model.mlp.SlRep msg = (eu.driver.model.mlp.SlRep) SpecificData
						.get().deepCopy(eu.driver.model.mlp.SlRep.SCHEMA$, receivedMessage);
				record.setRecordJson(msg.toString());
			} else if (receivedMessage.getSchema().getName()
					.equalsIgnoreCase("FeatureCollection")) {
				try {
					eu.driver.model.geojson.FeatureCollection msg = (eu.driver.model.geojson.FeatureCollection) SpecificData
							.get().deepCopy(
									eu.driver.model.geojson.FeatureCollection.SCHEMA$, receivedMessage);
					record.setRecordJson(msg.toString());
					record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
					List<eu.driver.model.geojson.Feature> featureList = msg.getFeatures();
					for (eu.driver.model.geojson.Feature feature : featureList) {
						Object properties = feature.getProperties();
						Utf8 imageRefUTF8 = new Utf8("image_ref");
						Utf8 nameUTF8 = new Utf8("name");
						try {
							if (properties instanceof HashMap) {
								HashMap<Utf8, Utf8> prop = (HashMap<Utf8, Utf8>)properties;
								
								Utf8 tmpUtf8 = prop.get(imageRefUTF8);
								if (tmpUtf8 != null) {
									String imageRef = tmpUtf8.toString();
						    		if (imageRef != null) {
										int lastIdx = imageRef.lastIndexOf("/");
										String fileName = imageRef.substring(lastIdx+1);
										
										Attachment attachment = new Attachment();
										attachment.setRecord(record);
										attachment.setName("record/attachements/" + fileName);
										attachment.setUrl(imageRef);
										record.addAttachment(attachment);
									}
								}
								tmpUtf8 = prop.get(nameUTF8);
								if (tmpUtf8 != null) {
									String name = tmpUtf8.toString();
									if (name != null) {
										if (record.getHeadline() == null) {
											record.setHeadline(name);
										}
									}
								}
							}
						} catch (Exception e) {
							log.error("Error evaluating the imageRef");
						}
					}
				} catch(Exception e) {
					eu.driver.model.geojson.photo.FeatureCollection msg = (eu.driver.model.geojson.photo.FeatureCollection) SpecificData
							.get().deepCopy(
									eu.driver.model.geojson.photo.FeatureCollection.SCHEMA$, receivedMessage);
					record.setRecordJson(msg.toString());
					record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
					
					List<Feature> featureList = msg.getFeatures();
					for (Feature feature : featureList) {
						properties properties = feature.getProperties();
						if (record.getHeadline() == null) {
							if (properties.getMissionName() != null) {
								record.setHeadline(properties.getMissionName().toString());	
							}
						}
						List<files> files = properties.getFiles();
						for (files file : files) {
							try {
								String storeName = "";
								String fileName = "";
								if (file.getUrl() != null) {
									String url = file.getUrl().toString();
									if (url.length() > 0) {
										int lastIdx = url.lastIndexOf("/");
										fileName = url.substring(lastIdx+1);
										url = url.substring(0,lastIdx);
										lastIdx = url.lastIndexOf("/");
										storeName += url.substring(lastIdx+1);
										
										Attachment attachment = new Attachment();
										attachment.setRecord(record);
										attachment.setName("record/attachements/" + storeName + "/" + fileName);
										attachment.setUrl(file.getUrl().toString());
										record.addAttachment(attachment);
									}
								}
							} catch (Exception ex) {
								log.error("Error loading and storing the message attachement: " + file.getUrl());
							}
						}
					}
				}
			} else if (receivedMessage.getSchema().getName()
					.equalsIgnoreCase("TSO_2_0")) {
				eu.driver.model.emsi.TSO_2_0 msg = (eu.driver.model.emsi.TSO_2_0) SpecificData
						.get().deepCopy(eu.driver.model.emsi.TSO_2_0.SCHEMA$, receivedMessage);
				record.setRecordJson(msg.toString());
				record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
				record.setHeadline(msg.getEVENT().getNAME().toString());
			} else if (receivedMessage.getSchema().getName()
					.equalsIgnoreCase("LargeDataUpdate")) {
				eu.driver.model.core.LargeDataUpdate msg = (eu.driver.model.core.LargeDataUpdate) SpecificData
						.get().deepCopy(
								eu.driver.model.core.LargeDataUpdate.SCHEMA$, receivedMessage);
				
				record.setRecordJson(msg.toString());
				record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
				record.setHeadline("New LargeFile available: " + msg.getTitle().toString());
				try {
					String storeName = "";
					if (msg.getUrl() != null) {
						String url = msg.getUrl().toString();
						if (url.length() > 0) {
							try {
								int lastIdx = url.lastIndexOf("/");
								storeName += url.substring(lastIdx+1);
							} catch (Exception ex) {
								log.error("Error loading the message attachement: " + msg.getUrl(), ex);
							}
							
							try {
								Attachment attachment = new Attachment();
								attachment.setRecord(record);
								attachment.setMimeType(msg.getDataType().toString());
								attachment.setName("record/attachements/" + storeName);
								attachment.setUrl(url);
								record.addAttachment(attachment);
							} catch (Exception ex) {
								log.error("Error loading and storing the message attachement: " + msg.getUrl());
							}
						}
					}
				} catch (Exception ex) {
					log.error("Error loading and storing the message attachement: " + msg.getUrl());
				}
			} else if (receivedMessage.getSchema().getName()
					.equalsIgnoreCase("GeoJSONEnvelope")) {
				eu.driver.model.geojson.GeoJSONEnvelope msg = (eu.driver.model.geojson.GeoJSONEnvelope) SpecificData
						.get().deepCopy(
								eu.driver.model.geojson.GeoJSONEnvelope.SCHEMA$, receivedMessage);
				record.setRecordJson(msg.toString());
				record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
			}  else if (receivedMessage.getSchema().getName()
					.equalsIgnoreCase("MapLayerUpdate")) {
				eu.driver.model.core.MapLayerUpdate msg = (eu.driver.model.core.MapLayerUpdate) SpecificData
						.get().deepCopy(
								eu.driver.model.core.MapLayerUpdate.SCHEMA$, receivedMessage);
				record.setRecordJson(msg.toString());
				record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
				record.setHeadline(msg.getTitle().toString() + ", " + msg.getUpdateType().toString());
			} else if (receivedMessage.getSchema().getName()
					.equalsIgnoreCase("SessionMgmt")) {
				eu.driver.model.core.SessionMgmt msg = (eu.driver.model.core.SessionMgmt) SpecificData
						.get().deepCopy(eu.driver.model.core.SessionMgmt.SCHEMA$, receivedMessage);
				record.setRecordJson(msg.toString());
				record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
				record.setHeadline(msg.getSessionId().toString() + ", " + msg.getSessionState().toString());
	
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
				Szenario szenario = null;
				for (Szenario tmpSzenario : trial.getSzenarioList()) {
					if (tmpSzenario.getSzenarioId().equals(szenarioId)) {
						szenario = tmpSzenario;
					}
				}
				if (szenario == null) {
					szenario = new Szenario();
					szenario.setTrial(trial);
					szenario.setSzenarioId(msg.getScenarioId().toString());
					szenario.setSzenarioName(msg.getScenarioName().toString());
					szenario.setStartDate(new Date());
					trial.addSzenario(szenario);
					trial.setEndDate(null);
				}
				if (msg.getSessionState() == SessionState.STOP) {
					szenario.setEndDate(new Date());
				}
	
				String sessionId = msg.getSessionId().toString();
				Session session = null;
				for (Session tmpSession : szenario.getSessionList()) {
					if (tmpSession.getSessionId().equals(sessionId)) {
						session = tmpSession;
					}
				}
				if (session == null) {
					session = new Session();
					session.setSzenario(szenario);
					session.setSessionId(msg.getSessionId().toString());
					session.setSessionName(msg.getSessionName().toString());
					session.setStartDate(new Date());
					szenario.addSession(session);
					trial.setEndDate(null);
					szenario.setEndDate(null);
				}
				if (msg.getSessionState() == SessionState.STOP) {
					session.setEndDate(new Date());
				}
				// check if there is a actual trial that is not that trial.
				if (trial.getId() == null) {
					Trial actTrial = trialRepo.findActualTrial();
					if (actTrial != null) {
						actTrial.setActual(false);
						trialRepo.saveAndFlush(actTrial);
					}
				}
				trialRepo.saveAndFlush(trial);
			} else if (receivedMessage.getSchema().getName()
					.equalsIgnoreCase("PhaseMessage")) {
				eu.driver.model.core.PhaseMessage msg = (eu.driver.model.core.PhaseMessage) SpecificData
						.get().deepCopy(eu.driver.model.core.PhaseMessage.SCHEMA$, receivedMessage);
				record.setRecordJson(msg.toString());
				record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
				record.setHeadline(msg.getPhase().name() + ", state: " + msg.getIsStarting());
			} else if (receivedMessage.getSchema().getName()
					.equalsIgnoreCase("RolePlayerMessage")) {
				eu.driver.model.core.RolePlayerMessage msg = (eu.driver.model.core.RolePlayerMessage) SpecificData
						.get().deepCopy(eu.driver.model.core.RolePlayerMessage.SCHEMA$, receivedMessage);
				record.setRecordJson(msg.toString());
				record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
				record.setHeadline(msg.getHeadline().toString());
			} else if (receivedMessage.getSchema().getName()
					.equalsIgnoreCase("Timing")) {
				eu.driver.model.core.Timing msg = (eu.driver.model.core.Timing) SpecificData
						.get().deepCopy(eu.driver.model.core.Timing.SCHEMA$, receivedMessage);
				if (!msg.getState().equals(currentTimingState)
						|| msg.getTrialTimeSpeed() != currentTrialTimeSpeed) {
					this.currentTimingState = msg.getState();
					this.currentTrialTimeSpeed = msg.getTrialTimeSpeed();
					record.setRecordJson(msg.toString());
					record.setHeadline("New State: " + this.currentTimingState + ", speed: " + this.currentTrialTimeSpeed);
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
				record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
				record.setHeadline("New stage Report for session: "  + msg.getOstTrialSessionId().toString() + ", stage id: " + msg.getOstTrialStageId().toString());
			} else if (receivedMessage.getSchema().getName()
					.equalsIgnoreCase("ObserverToolAnswer")) {
				eu.driver.model.core.ObserverToolAnswer msg = (eu.driver.model.core.ObserverToolAnswer) SpecificData
						.get().deepCopy(
								eu.driver.model.core.ObserverToolAnswer.SCHEMA$, receivedMessage);
				String observer = msg.getObservationTypeName().toString().toUpperCase();
				record.setRecordJson(msg.toString());
				record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
				record.setHeadline("A new observation was reported by: " + observer);
				
				// check for Baseline/Innovation line run
				if (observer.indexOf(ANSWER_ALL_TRIAL) != -1) {
					record.setRunType(AARConstants.RECORD_RUN_TYPE_TRIALD);
				} else if (observer.indexOf(ANSWER_OBS_BL) != -1) {
					record.setRunType(AARConstants.RECORD_RUN_TYPE_BL);
				} else if (observer.indexOf(ANSWER_PRACT_FIE) != -1) {
					record.setRunType(AARConstants.RECORD_RUN_TYPE_FIE);
				} else if (observer.indexOf(ANSWER_PRACT_SOLUTION) != -1) {
					record.setRunType(AARConstants.RECORD_RUN_TYPE_SOLUTIOND);
				}
			} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("RequestStartInject")) {
				eu.driver.model.sim.request.RequestStartInject msg = (eu.driver.model.sim.request.RequestStartInject) SpecificData
						.get().deepCopy(eu.driver.model.sim.request.RequestStartInject.SCHEMA$, receivedMessage);
				record.setRecordJson(msg.toString());
				record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
				record.setHeadline("A new RequestStartInject was sent by: " + msg.getInject().toString());
			} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("Post")) {
				receivedMessage.put(14, null);
				eu.driver.model.sim.entity.Post msg = (eu.driver.model.sim.entity.Post) SpecificData
						.get().deepCopy(eu.driver.model.sim.entity.Post.SCHEMA$, receivedMessage);
				record.setRecordJson(msg.toString());
				record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
				record.setHeadline("A new email was sent by: " + msg.getSenderName().toString());
				
				// check for Baseline/Innovation line run
	     		boolean inRun = false;
				boolean blRun = false;
				if (msg.getSenderName().toString().indexOf(POST_BL_EXT) != -1) {
					blRun = true;
				}
				
				List<CharSequence> recipients = msg.getRecipients();
				if (recipients != null && recipients.size() > 0) {
					for (CharSequence recipient: recipients) {
						if (recipient.toString().indexOf(POST_BL_EXT) != -1) {
							blRun = true;
						} else if (!recipient.toString().equalsIgnoreCase("RolePlayer")) {
							inRun = true;
						}
					}
				}
				if (inRun && blRun) {
					record.setRunType(AARConstants.RECORD_RUN_TYPE_BOTH);
				} else if (blRun) {
					record.setRunType(AARConstants.RECORD_RUN_TYPE_BL);
				}
			} else {
				// unknown data
				record = null;
				log.error("Unknown message received: " + topicName);
				log.error(receivedMessage);
			}
		} catch (Exception e) {
			log.error("Error evaluation the message", e);
		}

		if (record != null) {
			try {
				record = recordRepo.saveAndFlush(record);
				if (record.getRecordType() != "Log" && record.getRecordType() != "ObserverToolAnswer") {
					LocalDateTime localDate = LocalDateTime.now();
					Date sendDate = Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
					WSRecordNotification notification = new WSRecordNotification(
							record.getId(), record.getClientId(),
							record.getTopic(), sendDate,
							record.getRecordType(), record.getHeadline(), record.getMsgType(), record.getRunType(), null, null);
					
					WSController.getInstance().sendMessage(
							mapper.objectToJSONString(notification));
				}
			} catch (Exception e) {
				log.error("Error processing the message!", e);
			}
		}
	}
	
	@ApiOperation(value = "finishUpTheTrial", nickname = "finishUpTheTrial")
	@RequestMapping(value = "/AARService/finishUpTheTrial", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> finishUpTheTrial() {
		log.info("-->finishUpTheTrial");		
		Boolean result = false;
		
		List<Attachment> attachments = attachmentRecord.findAll();
		
		for (Attachment attachment : attachments) {
			String url = attachment.getUrl();
			
			if (url != null) {
				String filePath = ".";
				String storeName = attachment.getName();
				storeName = storeName.replaceAll("//", "/");
				
				try {
				    Path recordDir = null; 
				    StringTokenizer tokens = new StringTokenizer(storeName, "/");
				    boolean fileExisting = true;
					
					while (tokens.hasMoreTokens()) {
						filePath +=  "/" + tokens.nextToken();
						recordDir = Paths.get(filePath); 
					    if (tokens.hasMoreTokens()) {
					    	if (Files.notExists(recordDir)) { 
						        try { 
						        	Files.createDirectory(recordDir);
						        	fileExisting = false;
						        } catch (Exception e ) {
						        	log.error("Error creating the " + filePath + " directory.", e);
						        	throw e;
						        }
						    }
					    } 
					}
				    
					if (!fileExisting) {
						InputStream in = new java.net.URL(url).openStream();
						Files.copy(in, recordDir, StandardCopyOption.REPLACE_EXISTING);
					}
				} catch (Exception ex) {
					log.error("Error loading the message attachement: " + url);
				}
			}
		}
		
		log.info("finishUpTheTrial-->");
		return new ResponseEntity<Boolean>(result, HttpStatus.OK);
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
			@RequestParam(value="size", required=false) Integer size, HttpSession httpSession) {
		log.info("-->getAllRecords");
		String query = "SELECT NEW Record(i.id, i.clientId, i.sessionId, i.topic, i.recordType, i.createDate, i.trialDate, i.headline, i.msgType, i.runType) FROM Record i";

		
		String sessionId = httpSession.getId();
		// create the query using the active Filter
		RecordFilter actualFilter = this.sessionFilters.get(sessionId);
		if (actualFilter == null) {
			actualFilter = new RecordFilter();
			this.sessionFilters.put(sessionId, actualFilter);
		} else {
			if (actualFilter.isFilterEnabled()) {
				query += this.createFilterQuery(httpSession);
			}
		}
		
		query += " ORDER BY i.createDate DESC";

		TypedQuery<Record> typedQuery = entityManager.createQuery(query, Record.class);
		if (actualFilter.getFromDate() != null) {
			typedQuery.setParameter("fromDate", actualFilter.getFromDate(), TemporalType.TIMESTAMP);
		}
		if (actualFilter.getToDate() != null) {
			typedQuery.setParameter("toDate", actualFilter.getToDate(), TemporalType.TIMESTAMP);	
		}
		
		if (actualFilter.getScenarioId() != null) {
			Szenario szenario = szenarioRepo.findObjectBySzenarioId(actualFilter.getScenarioId());
			typedQuery.setParameter("fromDate", szenario.getStartDate(), TemporalType.TIMESTAMP);
			if (szenario.getEndDate() != null) {
				typedQuery.setParameter("toDate", szenario.getEndDate(), TemporalType.TIMESTAMP);	
			} else {
				typedQuery.setParameter("toDate",  new Date(), TemporalType.TIMESTAMP);	
			}
		} else if (actualFilter.getSessionId() != null) {
			Session session = sessionRepo.findObjectBySessionId(actualFilter.getSessionId());
			typedQuery.setParameter("fromDate", session.getStartDate(), TemporalType.TIMESTAMP);
			if (session.getEndDate() != null) {
				typedQuery.setParameter("toDate", session.getEndDate(), TemporalType.TIMESTAMP);	
			} else {
				typedQuery.setParameter("toDate",  new Date(), TemporalType.TIMESTAMP);	
			}
		}
		
		// use the filtering here
		if (page != null) {
			page--;
			if (size == null) {
				size = 20;
			}
			typedQuery.setFirstResult(page * size);
			typedQuery.setMaxResults(size);
			
			this.currentPageSize = Double.valueOf(size);
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
		log.info("-->getRecord");
		Record record = recordRepo.findObjectById(id);

		log.info("getRecord-->");
		return new ResponseEntity<Record>(record, HttpStatus.OK);
	}
	
	@ApiOperation(value = "getPageCount", nickname = "getPageCount")
	@RequestMapping(value = "/AARService/getPageCount", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = ArrayList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ArrayList.class),
			@ApiResponse(code = 500, message = "Failure", response = ArrayList.class) })
	public ResponseEntity<Double> getPageCount(HttpSession httpSession) {
		log.info("-->getPageCount");
		String query = "SELECT NEW Record(i.id) FROM Record i";

		String sessionId = httpSession.getId();
		// create the query using the active Filter
		RecordFilter actualFilter = this.sessionFilters.get(sessionId);
		if (actualFilter == null) {
			actualFilter = new RecordFilter();
			this.sessionFilters.put(sessionId, actualFilter);
		} else {
			if (actualFilter.isFilterEnabled()) {
				query += this.createFilterQuery(httpSession);
			}
		}
		
		TypedQuery<Record> typedQuery = entityManager.createQuery(query, Record.class);
		if (actualFilter.getFromDate() != null) {
			typedQuery.setParameter("fromDate", actualFilter.getFromDate(), TemporalType.TIMESTAMP);
		}
		if (actualFilter.getToDate() != null) {
			typedQuery.setParameter("toDate", actualFilter.getToDate(), TemporalType.TIMESTAMP);	
		}
		
		if (actualFilter.getScenarioId() != null) {
			Szenario szenario = szenarioRepo.findObjectBySzenarioId(actualFilter.getScenarioId());
			typedQuery.setParameter("fromDate", szenario.getStartDate(), TemporalType.TIMESTAMP);
			if (szenario.getEndDate() != null) {
				typedQuery.setParameter("toDate", szenario.getEndDate(), TemporalType.TIMESTAMP);	
			} else {
				typedQuery.setParameter("toDate",  new Date(), TemporalType.TIMESTAMP);	
			}
		} else if (actualFilter.getSessionId() != null) {
			Session session = sessionRepo.findObjectBySessionId(actualFilter.getSessionId());
			typedQuery.setParameter("fromDate", session.getStartDate(), TemporalType.TIMESTAMP);
			if (session.getEndDate() != null) {
				typedQuery.setParameter("toDate", session.getEndDate(), TemporalType.TIMESTAMP);	
			} else {
				typedQuery.setParameter("toDate",  new Date(), TemporalType.TIMESTAMP);	
			}
		}
		List<Record> records = typedQuery.getResultList();
		
		Double recCount = Double.valueOf(records.size());
		
		Double pageCount = 1D;
		if (recCount > 0 && this.currentPageSize > 0) {
			pageCount = Math.ceil(recCount/this.currentPageSize);
		}

		log.info("getPageCount-->");
		return new ResponseEntity<Double>(pageCount, HttpStatus.OK);
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
		
		if (trial == null) {
			// create Dummy Trial
			trial = new Trial();
			trial.setActual(true);
			trial.setStartDate(new Date());
			trial.setTrialId("Temp_1");
			trial.setTrialName("Trial");
		}

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
	public ResponseEntity<List<Record>> getAllTimelineRecords(HttpSession httpSession) {
		log.info("-->getAllTimelineRecords");

		String query = "SELECT NEW Record(i.id, i.topic, i.recordType, i.createDate, i.headline, i.msgType) FROM Record i";
		
		String sessionId = httpSession.getId();
		// create the query using the active Filter
		RecordFilter actualFilter = this.sessionFilters.get(sessionId);
		if (actualFilter == null) {
			actualFilter = new RecordFilter();
			this.sessionFilters.put(sessionId, actualFilter);
		} 
		
		if (actualFilter.isFilterEnabled()) {
			query += this.createFilterQuery(httpSession);
			query += "	AND";
		} else {
			query += "	WHERE";
		}
		
		query += " i.recordType != 'ObserverToolAnswer' and i.recordType != 'SessionMgmt'";
		
		query += " ORDER BY i.createDate DESC";

		TypedQuery<Record> typedQuery = entityManager.createQuery(query, Record.class);
		if (actualFilter.getFromDate() != null) {
			typedQuery.setParameter("fromDate", actualFilter.getFromDate(), TemporalType.TIMESTAMP);
		}
		if (actualFilter.getToDate() != null) {
			typedQuery.setParameter("toDate", actualFilter.getToDate(), TemporalType.TIMESTAMP);	
		}
		
		if (actualFilter.getScenarioId() != null) {
			Szenario szenario = szenarioRepo.findObjectBySzenarioId(actualFilter.getScenarioId());
			typedQuery.setParameter("fromDate", szenario.getStartDate(), TemporalType.TIMESTAMP);
			if (szenario.getEndDate() != null) {
				typedQuery.setParameter("toDate", szenario.getEndDate(), TemporalType.TIMESTAMP);	
			} else {
				typedQuery.setParameter("toDate",  new Date(), TemporalType.TIMESTAMP);	
			}
		} else if (actualFilter.getSessionId() != null) {
			Session session = sessionRepo.findObjectBySessionId(actualFilter.getSessionId());
			typedQuery.setParameter("fromDate", session.getStartDate(), TemporalType.TIMESTAMP);
			if (session.getEndDate() != null) {
				typedQuery.setParameter("toDate", session.getEndDate(), TemporalType.TIMESTAMP);	
			} else {
				typedQuery.setParameter("toDate",  new Date(), TemporalType.TIMESTAMP);	
			}
		}
		
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
	public ResponseEntity<RecordFilter> getActualFilter(HttpSession httpSession) {
		log.info("-->getActualFilter");
		String sessionId = httpSession.getId();
		// create the query using the active Filter
		RecordFilter actualFilter = this.sessionFilters.get(sessionId);
		if (actualFilter == null) {
			actualFilter = new RecordFilter();
			this.sessionFilters.put(sessionId, actualFilter);
		}
		log.info("getActualFilter-->");
		return new ResponseEntity<RecordFilter>(actualFilter,
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
			@RequestBody RecordFilter filter, HttpSession httpSession) {
		log.info("-->setActualFilter");

		String sessionId = httpSession.getId();
		this.sessionFilters.put(sessionId, filter);

		log.info("setActualFilter-->");
		return new ResponseEntity<RecordFilter>(filter,
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
	
	@ApiOperation(value = "getMsgTypes", nickname = "getMsgTypes")
	@RequestMapping(value = "/AARService/getMsgTypes", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = ArrayList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ArrayList.class),
			@ApiResponse(code = 500, message = "Failure", response = ArrayList.class) })
	public ResponseEntity<List<String>> getMsgTypes() {
		log.info("-->getMsgTypes");
		List<String> records = new ArrayList<String>();
		
		records.add(AARConstants.RECORD_MSG_TYPE_ACK);
		records.add(AARConstants.RECORD_MSG_TYPE_INFO);
		records.add(AARConstants.RECORD_MSG_TYPE_WARN);
		records.add(AARConstants.RECORD_MSG_TYPE_ERROR);
		records.add(AARConstants.RECORD_MSG_TYPE_EVAL);

		log.info("getMsgTypes-->");
		return new ResponseEntity<List<String>>(records, HttpStatus.OK);
	}
	
	@ApiOperation(value = "getRunTypes", nickname = "getRunTypes")
	@RequestMapping(value = "/AARService/getRunTypes", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = ArrayList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ArrayList.class),
			@ApiResponse(code = 500, message = "Failure", response = ArrayList.class) })
	public ResponseEntity<List<String>> getRunTypes() {
		log.info("-->getRunTypes");
		List<String> records = new ArrayList<String>();
		
		records.add(AARConstants.RECORD_RUN_TYPE_BL);
		records.add(AARConstants.RECORD_RUN_TYPE_IN);
		records.add(AARConstants.RECORD_RUN_TYPE_BOTH);
		records.add(AARConstants.RECORD_RUN_TYPE_FIE);
		records.add(AARConstants.RECORD_RUN_TYPE_SOLUTIOND);
		records.add(AARConstants.RECORD_RUN_TYPE_TRIALD);

		log.info("getRunTypes-->");
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
	public ResponseEntity<byte[]> createSequenceDiagram(HttpSession httpSession) {
		log.info("-->createSequenceDiagram");
		ByteArrayOutputStream bous = new ByteArrayOutputStream();
		String query = "SELECT NEW Record(i.id, i.clientId, i.topic, i.recordType, i.createDate, i.headline, i.msgType) FROM Record i";
		
		String sessionId = httpSession.getId();
		// create the query using the active Filter
		RecordFilter actualFilter = this.sessionFilters.get(sessionId);
		if (actualFilter == null) {
			actualFilter = new RecordFilter();
			this.sessionFilters.put(sessionId, actualFilter);
		} else {
			if (actualFilter.isFilterEnabled()) {
				query += this.createFilterQuery(httpSession);
			}
		}
		
		query += " ORDER BY i.createDate ASC";
		TypedQuery<Record> typedQuery = entityManager.createQuery(query, Record.class);
		if (actualFilter.getFromDate() != null) {
			typedQuery.setParameter("fromDate", actualFilter.getFromDate(), TemporalType.TIMESTAMP);
		}
		if (actualFilter.getToDate() != null) {
			typedQuery.setParameter("toDate", actualFilter.getToDate(), TemporalType.TIMESTAMP);	
		}
		
		if (actualFilter.getScenarioId() != null) {
			Szenario szenario = szenarioRepo.findObjectBySzenarioId(actualFilter.getScenarioId());
			typedQuery.setParameter("fromDate", szenario.getStartDate(), TemporalType.TIMESTAMP);
			if (szenario.getEndDate() != null) {
				typedQuery.setParameter("toDate", szenario.getEndDate(), TemporalType.TIMESTAMP);	
			} else {
				typedQuery.setParameter("toDate",  new Date(), TemporalType.TIMESTAMP);	
			}
		} else if (actualFilter.getSessionId() != null) {
			Session session = sessionRepo.findObjectBySessionId(actualFilter.getSessionId());
			typedQuery.setParameter("fromDate", session.getStartDate(), TemporalType.TIMESTAMP);
			if (session.getEndDate() != null) {
				typedQuery.setParameter("toDate", session.getEndDate(), TemporalType.TIMESTAMP);	
			} else {
				typedQuery.setParameter("toDate",  new Date(), TemporalType.TIMESTAMP);	
			}
		}
		
		List<Record> records = typedQuery.getResultList();
		if (records != null && records.size() > 0) {
			List<TopicReceiver> receivers = topicReceiverRepo.findAll();
			String source = createSequenceDiagramString(records, receivers);
			UUID uuid = UUID.randomUUID();
	        String fileName = uuid.toString();
			try {
				PrintStream out = new PrintStream(new FileOutputStream(fileName + ".txt"));
				out.print(source);
				out.close();
			} catch (Exception e) {
			    log.warn("Could not write the plantUML file!");
			}
			SourceStringReader reader = new SourceStringReader(source);
		    // Write the first image to "png"
			String desc = null;
		    try {
				desc = reader.generateImage(bous, new FileFormatOption(FileFormat.SVG));
			} catch (IOException e) {
				log.error("Error creating the sequence diagram!");
			}
		    // Return a null string if no generation
		    byte[] media = bous.toByteArray();
		    
		    try {
		    	FileOutputStream fos = new FileOutputStream(fileName + ".svg");
		    	fos.write(media);
		    	fos.close();
		    } catch (Exception e) {
		    	log.warn("Could not write the plantUML file!");
	    	}
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.valueOf("image/svg+xml"));
		    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
		    log.info("createSequenceDiagram-->");
		    return new ResponseEntity<byte[]>(media, headers, HttpStatus.OK);
		}
		log.info("createSequenceDiagram-->");
		return new ResponseEntity<byte[]>("".getBytes(), HttpStatus.OK);
	}
	
	@ApiOperation(value = "createOverviewSequenceDiagram", nickname = "createOverviewSequenceDiagram")
	@RequestMapping(value = "/AARService/createOverviewSequenceDiagram", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = byte[].class),
			@ApiResponse(code = 400, message = "Bad Request", response = byte[].class),
			@ApiResponse(code = 500, message = "Failure", response = byte[].class) })
	public ResponseEntity<byte[]> createOverviewSequenceDiagram(HttpSession httpSession) {
		log.info("-->createOverviewSequenceDiagram");
		ByteArrayOutputStream bous = new ByteArrayOutputStream();
		Long recCount = recordRepo.countRecordsWithoutLog();
		String query = "SELECT NEW Record(i.id, i.clientId, i.topic, i.recordType, i.createDate, i.headline, i.msgType) FROM Record i WHERE i.recordType != 'Log'";
		query += " ORDER BY i.createDate DESC";
		TypedQuery<Record> typedQuery = entityManager.createQuery(query, Record.class);
		typedQuery.setFirstResult(recCount.intValue()-20);
		typedQuery.setMaxResults(20);
		List<Record> records = typedQuery.getResultList();
		if (records != null && records.size() > 0) {
			List<TopicReceiver> receivers = topicReceiverRepo.findAll();
			String source = createSequenceDiagramString(records, receivers);
			SourceStringReader reader = new SourceStringReader(source);
		    // Write the first image to "png"
			String desc = null;
		    try {
				desc = reader.generateImage(bous, new FileFormatOption(FileFormat.SVG));
			} catch (IOException e) {
				log.error("Error creating the sequence diagram!");
			}
		    // Return a null string if no generation
		    byte[] media = bous.toByteArray();
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.valueOf("image/svg+xml"));
		    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
		    log.info("createSequenceDiagram-->");
		    return new ResponseEntity<byte[]>(media, headers, HttpStatus.OK);
		}
		log.info("createOverviewSequenceDiagram-->");
		return new ResponseEntity<byte[]>("".getBytes(), HttpStatus.OK);
	}

	@ApiOperation(value = "exportData", nickname = "exportData")
	@RequestMapping(value = "/AARService/exportData", method = RequestMethod.GET)
	@ApiImplicitParams({ @ApiImplicitParam(name = "exportType", value = "the type how the export should be created", required = true, dataType = "string", paramType = "query", allowableValues="SQL,CSV") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = RecordFilter.class),
			@ApiResponse(code = 400, message = "Bad Request", response = RecordFilter.class),
			@ApiResponse(code = 500, message = "Failure", response = RecordFilter.class) })
	public ResponseEntity<byte[]> exportData(@QueryParam("exportType")String exportType) {
		log.info("-->exportData");
		
		if (!(Files.exists(Paths.get("./record")))) {
            try {
				Files.createDirectories(Paths.get("./record"));
			} catch (IOException e) {
				log.error("Error creating the ./record directory!");
			}
        }
		
		StringBuffer exportBuffer = new StringBuffer();
		
		List<Trial> trials =  trialRepo.findAll();
		for (Trial trial : trials) {
			exportBuffer.append(trial.createBackupString(exportType));
		}
		
		List<TopicReceiver> topicReceivers = topicReceiverRepo.findAll();
		for (TopicReceiver topicReceiver : topicReceivers) {
			exportBuffer.append(topicReceiver.createBackupString(exportType));
		}
		
		List<Record> records = recordRepo.findAll();
		for (Record record : records) {
			exportBuffer.append(record.createBackupString(exportType));
		}
		
		byte[] fileContent = null;
		String fileName = "";
		
		try {
			PrintStream out = null;
			
			if (exportType.equalsIgnoreCase(AARConstants.BACKUP_TYPE_SQL)) {
				fileName = "./record/export.sql";
			} else if (exportType.equalsIgnoreCase(AARConstants.BACKUP_TYPE_CSV)) {
				fileName = "./record/export.csv";
			}
			out = new PrintStream(new FileOutputStream(fileName));
			
			out.print(exportBuffer.toString());
			out.close();
			
		} catch (Exception e) {
		    log.warn("Could not write the export file!");
		}
		
		try {
			this.pack("./record",  "export.zip");
			File file = new File("export.zip");
			
			try {
				fileContent = Files.readAllBytes(file.toPath());
			} catch (IOException e) {
				log.error("Error loading the file!", e);
			}
		} catch (IOException ie) {
			log.error("error creating the zip file");
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("application/zip"));
		headers.setContentDispositionFormData("attachment", "export.zip"); 
	    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
	    log.info("exportData-->");
	    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
	}
	
	@ApiOperation(value = "uploadOSTRecords", nickname = "uploadOSTRecords")
	@RequestMapping(value = "/AARService/uploadOSTRecords", method = RequestMethod.POST)
	@ApiImplicitParams({ @ApiImplicitParam(name = "ostCSVRecords", value = "the ostCSVRecords that should imported", required = true, dataType = "string", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = RecordFilter.class),
			@ApiResponse(code = 400, message = "Bad Request", response = RecordFilter.class),
			@ApiResponse(code = 500, message = "Failure", response = RecordFilter.class) })
	public ResponseEntity<Boolean> uploadOSTRecords(@RequestBody String ostCSVRecords) {
		log.info("-->uploadOSTRecords");
		SimpleDateFormat ostFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			String[] lines = null;
			if (ostCSVRecords.indexOf("\r\n") > -1) {
				lines = ostCSVRecords.split("\r\n");
			} else {
				lines = ostCSVRecords.split("\n");
			}
			
			if (lines != null) {
				// TrialID;TrialName;TrialSession_ID;SentTime;User;Role;ObservationType_ID;ObservationType;When;TrialTime;Question;Answer;Comment;Location;Attachment_URI;Attachment_Comment;Delete_Comment;Comment;Removal_reason;Overall_comment
				for (String line : lines) {
					String[] records = line.split(";");
					if (records != null && records.length >= 11) {
						try {
							ObserverToolAnswer answerMsg = new ObserverToolAnswer();
							answerMsg.setTrialId(Integer.parseInt(records[0]));
							answerMsg.setSessionId(Integer.parseInt(records[2]));
							answerMsg.setAnswerId(1);
							answerMsg.setTimeSendUTC(ostFormat.parse(records[3]).getTime()+(2*60*60*1000));
							answerMsg.setTimeWhen(ostFormat.parse(records[8]).getTime()+(2*60*60*1000));
							answerMsg.setObservervationTypeId(Integer.parseInt(records[6]));
							answerMsg.setObservationTypeName(records[7]);
							answerMsg.setObservationTypeDescription(records[7]);
							answerMsg.setDescription(records[7]);
							answerMsg.setMultiplicity(false);
							
							Question question = new Question();
							question.setId(1);
							question.setName(records[10]);
							question.setDescription(records[10]);
							question.setAnswer(records[11]);
							if (records.length >= 20) {
								question.setComment(records[19]);	
							} else {
								question.setComment("");
							}
							question.setTypeOfQuestion(TypeOfQuestion.text);

							List<Question> questions = new ArrayList<Question>();
							questions.add(question);
							answerMsg.setQuestions(questions);
							
							Record record = new Record();
							record.setCreateDate(new Date(answerMsg.getTimeSendUTC()));
							record.setTrialDate(new Date(answerMsg.getTimeSendUTC()));
							record.setRecordType(answerMsg.getSchema().getName());
							
							record.setClientId("TB-Ost");
							record.setTopic(TopicConstants.OST_ANSWER_TOPIC);

							record.setTrialDate(new Date(answerMsg.getTimeSendUTC()));
							record.setRecordJson(answerMsg.toString());
							
							if (record != null) {
								try {
									record = recordRepo.saveAndFlush(record);
									// check if the record needs to be send via the websocket
									boolean sendRecord = true;
									// ToDo: check if record meets filter criteras, if yes, push it to the client
									/*if (this.actualFilter != null && this.actualFilter.isFilterEnabled()) {
										// check if record meets filter criteria
										sendRecord = this.actualFilter.meetsRecordFilter(record);
									}*/
									
									if (sendRecord) {
										WSRecordNotification notification = new WSRecordNotification(
												record.getId(), record.getClientId(),
												record.getTopic(), record.getCreateDate(),
												record.getRecordType(), record.getHeadline(), record.getMsgType(), record.getRunType(), null, null);
										
										WSController.getInstance().sendMessage(
												mapper.objectToJSONString(notification));	
									}
								} catch (Exception e) {
									log.error("Error processing the message!", e);
								}
							}
						} catch (Exception e) {
							log.error("Error creating OST Answer Record!", e);
						}
					}
				}
			}
		} catch (Exception ex) {
			log.error("Error importing OST Answers!", ex);
		}
		
		log.info("uploadOSTRecords-->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "importData", nickname = "importData")
	@RequestMapping(value = "/AARService/importData", method = RequestMethod.POST)
	@ApiImplicitParams({ @ApiImplicitParam(name = "importData", value = "the records that should imported", required = true, dataType = "string", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = RecordFilter.class),
			@ApiResponse(code = 400, message = "Bad Request", response = RecordFilter.class),
			@ApiResponse(code = 500, message = "Failure", response = RecordFilter.class) })
	@Transactional
	public ResponseEntity<Boolean> importData(@RequestBody String importData) {
		log.info("-->importData");
		
		try {
			String[] lines = null;
			if (importData.indexOf("\r\n") > -1) {
				lines = importData.split("\r\n");
			} else {
				lines = importData.split("\n");
			}
			
			if (lines != null) {
				for (String line : lines) {
					log.info(line);
					try {
						Query query = entityManager.createNativeQuery(line);
						query.executeUpdate();	
					} catch (Exception e) {
						log.error("Error inserting record into DB!");
					}
				}
			}
		} catch (Exception ex) {
			log.error("Error importing records to DB!", ex);
		}
		
		log.info("importData-->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "analyseRecords", nickname = "analyseRecords")
	@RequestMapping(value = "/AARService/analyseRecords", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = RecordFilter.class),
			@ApiResponse(code = 400, message = "Bad Request", response = RecordFilter.class),
			@ApiResponse(code = 500, message = "Failure", response = RecordFilter.class) })
	public ResponseEntity<Boolean> analyseRecords() {
		log.info("-->analyseRecords");
		
		List<Record> records = recordRepo.findAll();
		
		for (Record record : records) {
			/*if (record.getClientId().equalsIgnoreCase("TB-TrialMgmt") || 
					record.getClientId().equalsIgnoreCase("TB-AARTool")	||
					record.getClientId().equalsIgnoreCase("TB-Ost") ||
					record.getClientId().equalsIgnoreCase("TB-TimeService")) {
				record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
				recordRepo.saveAndFlush(record);
				if (record.getClientId().equalsIgnoreCase("TB-TrialMgmt")) {
					try {
						InputStream input = new ByteArrayInputStream(record.getRecordJson().getBytes());
						DataInputStream din = new DataInputStream(input);
						Schema parsedSchema = SessionMgmt.SCHEMA$;
						Decoder decoder = DecoderFactory.get().jsonDecoder(parsedSchema, din);
					    DatumReader<GenericData.Record> reader = new GenericDatumReader(parsedSchema);
					    GenericRecord genRecord = reader.read(null, decoder);
					    
					    SessionMgmt sessMgmt = new SessionMgmt();
					    //SessionMgmt msg = 
					} catch (Exception Ex) {
						log.error("Error sending large data update message!", Ex);
						return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
				
			} else */if (record.getRecordType().equalsIgnoreCase("Alert")) {
				try {
					JSONObject jsonObj = new JSONObject(record.getRecordJson());
					if (record.getHeadline() == null) {
						if (jsonObj.getString("msgType").equalsIgnoreCase("Ack")) {
							record.setMsgType(AARConstants.RECORD_MSG_TYPE_ACK);
							record.setHeadline(jsonObj.getString("sender") + " + " + jsonObj.getString("references"));
						} else if (jsonObj.getString("msgType").equalsIgnoreCase("Error")) {
							record.setMsgType(AARConstants.RECORD_MSG_TYPE_ERROR);
							record.setHeadline(jsonObj.getString("sender") + " + " + jsonObj.getString("note"));
						} else {
							record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
							if (jsonObj.get("info") instanceof JSONArray) {
								record.setHeadline(jsonObj.getJSONArray("info").getJSONObject(0).getString("headline"));
							} else {
								record.setHeadline(jsonObj.getJSONObject("info").getString("headline"));
							}
						}
					}
					if (jsonObj.get("info") instanceof JSONArray) {
						JSONArray infos = jsonObj.getJSONArray("info");
						int count = infos.length();
						for (int a = 0; a < count; a++)  {
							JSONObject info = infos.getJSONObject(a);
							if (info.get("resource") instanceof JSONArray) {
								JSONArray resources = info.getJSONArray("resource");
								int rCount = resources.length();
								for (int i = 0; i < rCount; i++)  {
									JSONObject resource = resources.getJSONObject(i);
									if (resource.getString("uri") != null) {
										Attachment attachment = new Attachment();
										attachment.setRecord(record);
										attachment.setUrl(resource.getString("uri"));
										attachment.setMimeType(resource.getString("mimeType"));
										if (attachment.getMimeType().equalsIgnoreCase("image/png")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/" + a + "/" + i + "/file.png");	
										} else if (attachment.getMimeType().equalsIgnoreCase("image/jpeg")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/" + a + "/" + i + "/file.jpeg");
										} else if (attachment.getMimeType().equalsIgnoreCase("image/jpg")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/" + a + "/" + i + "/file.jpg");
										} else if (attachment.getMimeType().equalsIgnoreCase("application/pdf")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/" + a + "/" + i + "/file.pdf");
										}
										
										// check i attachment allready exists
										if (!record.existsAttachment(attachment.getName())) {
											record.addAttachment(attachment);
										}
									}
								}
							} else {
								try {
									JSONObject resource = info.getJSONObject("resource");
									if (resource != null && resource.getString("uri") != null) {
										Attachment attachment = new Attachment();
										attachment.setRecord(record);
										attachment.setUrl(resource.getString("uri"));
										attachment.setMimeType(resource.getString("mimeType"));
										if (attachment.getMimeType().equalsIgnoreCase("image/png")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/" + a + "/" + 0 + "/file.png");	
										} else if (attachment.getMimeType().equalsIgnoreCase("image/jpeg")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/" + a + "/" + 0 + "/file.jpeg");
										} else if (attachment.getMimeType().equalsIgnoreCase("image/jpg")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/" + a + "/" + 0 + "/file.jpg");
										} else if (attachment.getMimeType().equalsIgnoreCase("application/pdf")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/" + a + "/" + 0 + "/file.pdf");
										}
										
										// check i attachment allready exists
										if (!record.existsAttachment(attachment.getName())) {
											record.addAttachment(attachment);
										}
									}
								} catch (Exception e) {
									
								}
							}
						}
					} else {
						try {
						JSONObject info = jsonObj.getJSONObject("info");
							if (info != null && info.get("resource") instanceof JSONArray) {
								JSONArray resources = info.getJSONArray("resource");
								int rCount = resources.length();
								for (int i = 0; i < rCount; i++)  {
									JSONObject resource = resources.getJSONObject(i);
									if (resource.getString("uri") != null) {
										Attachment attachment = new Attachment();
										attachment.setRecord(record);
										attachment.setUrl(resource.getString("uri"));
										attachment.setMimeType(resource.getString("mimeType"));
										if (attachment.getMimeType().equalsIgnoreCase("image/png")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/0/" + i + "/file.png");	
										} else if (attachment.getMimeType().equalsIgnoreCase("image/jpg")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/0/" + i + "/file.jpg");
										} else if (attachment.getMimeType().equalsIgnoreCase("image/jpeg")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/0" + i + "/file.jpeg");
										} else if (attachment.getMimeType().equalsIgnoreCase("application/pdf")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/0/" + i + "/file.pdf");
										}
										
										// check i attachment allready exists
										if (!record.existsAttachment(attachment.getName())) {
											record.addAttachment(attachment);
										}
									}
								}
							} else {
								try {
									JSONObject resource = info.getJSONObject("resource");
									if (resource != null && resource.getString("uri") != null) {
										Attachment attachment = new Attachment();
										attachment.setRecord(record);
										attachment.setUrl(resource.getString("uri"));
										attachment.setMimeType(resource.getString("mimeType"));
										if (attachment.getMimeType().equalsIgnoreCase("image/png")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/0/0/file.png");	
										} else if (attachment.getMimeType().equalsIgnoreCase("image/jpg")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/0/0/file.jpg");
										} else if (attachment.getMimeType().equalsIgnoreCase("image/jpeg")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/0/0/file.jpeg");
										} else if (attachment.getMimeType().equalsIgnoreCase("application/pdf")) {
											attachment.setName("record/attachments/" + jsonObj.getString("identifier") + "/0/0/file.pdf");
										}
										
										// check i attachment allready exists
										if (!record.existsAttachment(attachment.getName())) {
											record.addAttachment(attachment);
										}
									}
								} catch (Exception e) {
									
								}
							}
						} catch (Exception e) {
							
						}
					}
					recordRepo.saveAndFlush(record);
				} catch (Exception e) {
					// ignore
					log.error("Error processing the record!");
				}
			}/* else if (record.getRecordType().equalsIgnoreCase("Log")){
				try {
					JSONObject jsonObj = new JSONObject(record.getRecordJson());
					if (jsonObj.getString("level").equalsIgnoreCase("ERROR")) {
						record.setMsgType(AARConstants.RECORD_MSG_TYPE_ERROR);
					} else if (jsonObj.getString("level").equalsIgnoreCase("Warn")) {
						record.setMsgType(AARConstants.RECORD_MSG_TYPE_WARN);
					} else {
						record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
					}
					record.setHeadline(jsonObj.getString("log"));
					recordRepo.saveAndFlush(record);
				} catch (Exception e) {
					// ignore
				}
			} else {
				record.setMsgType(AARConstants.RECORD_MSG_TYPE_INFO);
				recordRepo.saveAndFlush(record);
			}*/
		}
		
		log.info("analyseRecords-->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "downloadAttachment", nickname = "downloadAttachment")
	@RequestMapping(value = "/AARService/downloadAttachment", method = RequestMethod.GET)
	@ApiImplicitParams({ @ApiImplicitParam(name = "filename", value = "the attachment path", required = true, dataType = "string", paramType = "query") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = RecordFilter.class),
			@ApiResponse(code = 400, message = "Bad Request", response = RecordFilter.class),
			@ApiResponse(code = 500, message = "Failure", response = RecordFilter.class) })
	public ResponseEntity<byte[]> downloadAttachment(@QueryParam("filename")String filename) {
		log.info("-->downloadAttachment");
		
		byte[] fileContent = null;
		File file = new File(filename);
		
		try {
			fileContent = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			log.error("Error loading the file!", e);
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", filename); 
	    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
	    MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
	    String mime = mimeTypesMap.getContentType(file);
	    headers.setContentType(MediaType.parseMediaType(mime));

	    log.info("downloadAttachment-->");
	    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
		
	}
	
	@ApiOperation(value = "uploadBackup", nickname = "uploadBackup")
	@RequestMapping(value = "/AARService/uploadBackup", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "file", value = "the file to be uploaded", required = true, dataType = "__file")})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	@Transactional
	public ResponseEntity<String> uploadBackup(@RequestPart("file") MultipartFile file) {
		log.info("-->uploadBackup");
		String fileName = fileStorageService.storeFile("./record", file);
		
		if (fileName != null) {
			try {
				this.unzip(fileName, "./record");
				Path filePath = Paths.get("./record/export.sql");
				if (Files.exists(filePath)) {
					String content = new String(Files.readAllBytes(filePath));
					this.importData(content);
				}
			} catch (IOException e) {
				log.error("Error unzipping the backup!", e);
			}
		}
		
		log.info("uploadBackup-->");
		return new ResponseEntity<String>(fileName, HttpStatus.OK);
	}
	
	
	
	private String createFilterQuery(HttpSession httpSession) {
		String query = "";
		Boolean firstParam = true;
		
		String sessionId = httpSession.getId();
		// create the query using the active Filter
		RecordFilter actualFilter = this.sessionFilters.get(sessionId);
		if (actualFilter == null) {
			return query;
		}

		if (actualFilter.getId() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			}
			query += "i.id=" + actualFilter.getId();
		}

		if (actualFilter.getRecordType() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			} else {
				query += " AND ";
			}
			query += "i.recordType='" + actualFilter.getRecordType() + "'";
		}

		if (actualFilter.getTopicName() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			} else {
				query += " AND ";
			}
			query += "i.topic='" + actualFilter.getTopicName() + "'";
		}

		if (actualFilter.getSenderClientId() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			} else {
				query += " AND ";
			}
			query += "i.clientId='" + actualFilter.getSenderClientId() + "'";;
		}
		
		if (actualFilter.getMsgType() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			} else {
				query += " AND ";
			}
			query += "i.msgType='" + actualFilter.getMsgType() + "'";;
		}
		
		if (actualFilter.getRunType() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			} else {
				query += " AND ";
			}
			query += "i.runType='" + actualFilter.getRunType() + "'";;
		}

		if (actualFilter.getFromDate() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			} else {
				query += " AND ";
			}
			query += "i.createDate>:fromDate";
		}

		if (actualFilter.getToDate() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			} else {
				query += " AND ";
			}
			query += "i.createDate<:toDate";
		}
		
		if (actualFilter.getScenarioId() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			} else {
				query += " AND ";
			}
			query += "i.createDate>:fromDate AND i.createDate<:toDate";
		}
		
		if (actualFilter.getSessionId() != null) {
			if (firstParam) {
				query += " WHERE ";
				firstParam = false;
			} else {
				query += " AND ";
			}
			query += "i.createDate>:fromDate AND i.createDate<:toDate";
		}

		return query;

	}
	
	private String createSequenceDiagramString(List<Record> records, List<TopicReceiver> receivers) {
		log.info("-->createSequenceDiagramString");
		String data = this.createDiagramStart();
		
		String participants = "";
		
		data += "participant \"Testbed\" as Testbed #Thistle\n";
		data += "participant \"TB_TrialMgmt\" as TB_TrialMgmt #Thistle\n";
		data += "participant \"TB_Ost\" as TB_Ost #Thistle\n";
		data += "{participants}\n";
		
		Map<String, List<TopicReceiver>> receiverMap = new HashMap<String, List<TopicReceiver>>();

		for (TopicReceiver receiver : receivers) {
			List<TopicReceiver> topicReceiver = receiverMap.get(receiver.getTopicName());
			if (topicReceiver == null) {
				topicReceiver = new ArrayList<TopicReceiver>();
			}
			topicReceiver.add(receiver);
			receiverMap.put(receiver.getTopicName(), topicReceiver);
		}
		
		for (Record record : records) {
			String cis = "Testbed";
			String sender = record.getClientId();
			if (!record.getRecordType().equalsIgnoreCase("LOG") && !record.getRecordType().equalsIgnoreCase("TopicInvite")) {
				int idx = sender.indexOf("--");
				if (idx > -1) {
					sender = sender.substring(0, idx);
				}
				sender = sender.replaceAll(" ", "_");
				sender = sender.replaceAll("-", "_");
				sender = sender.replaceAll("\\.", "_");
				sender = sender.replaceAll(":", "_");
								
				if (record.getRecordType().equalsIgnoreCase("ObserverToolAnswer")) {
					try {
						Record specRecord = recordRepo.findObjectById(record.getId());
						if (specRecord.getRecordJson() != null) {
							JSONObject jsonRecord = new JSONObject(specRecord.getRecordJson());
							JSONArray questions = jsonRecord.getJSONArray("questions");
							int size = questions.length();
							String observerName = jsonRecord.getString("observationTypeName");
							if (observerName.indexOf(ANSWER_OBS_IN) != -1) {
								data += "rnote over TB_Ost #RoyalBlue\n";
							} else if (observerName.indexOf(ANSWER_OBS_BL) != -1) {
								data += "rnote over TB_Ost #RoyalBlue\n";
							} else if (observerName.indexOf(ANSWER_ALL_TRIAL) != -1) {
								data += "rnote over TB_Ost #RoyalBlue\n";
							} else {
								data += "rnote over TB_Ost #RoyalBlue\n";
							}
							
							data += "<color:white><b>" + jsonRecord.getString("observationTypeDescription") + "</b></color>\n";
							
							data += "<color:white>Observer: " + jsonRecord.getString("observationTypeName") + " reported: </color>\n";
							for (int i = 0; i < size; i++) {
								JSONObject question = questions.getJSONObject(i);
								data += "<color:white>* " + question.getString("name") + ": " + question.getString("answer") + " </color>\n";
								if (question.getString("comment") != null && question.getString("comment").length() > 0) {
									data += "<color:white> //comment: " + question.getString("comment").replaceAll("\n", " - ") + "//\n";	
								}
							}
						}
					} catch (Exception e) {
						log.error("Error creating the ObserverToolAnswer note!", e);
					}
					data += "endrnote\n";
				} else if (record.getRecordType().equalsIgnoreCase("RolePlayerMessage")) {
					data += "rnote over TB_TrialMgmt #LightSeaGreen\n";
					try {
						Record specRecord = recordRepo.findObjectById(record.getId());
						if (specRecord.getRecordJson() != null) {
							JSONObject jsonRecord = new JSONObject(specRecord.getRecordJson());
							data += "A new Roleplayer message was injected:\n";
							data += "Roleplayer: " + jsonRecord.getString("rolePlayerName") + ", type: " + jsonRecord.getString("type") + "\n";
							try {
								data += "title: " + jsonRecord.getString("title") + "\n";
							} catch (Exception e) { }
							try {
								data += "headline: " + jsonRecord.getString("hadline") + "\n";
							} catch (Exception e) { }
							try {
								data += "description: " + jsonRecord.getString("description") + "\n";
							} catch (Exception e) { }
						}
					} catch (Exception e) {
						log.error("Error creating the RolePlayerMessage note!", e);
					}
					data += "endrnote\n";
				} else if (record.getRecordType().equalsIgnoreCase("PhaseMessage")) {
					data += "rnote over TB_TrialMgmt #DimGray\n";
					try {
						Record specRecord = recordRepo.findObjectById(record.getId());
						if (specRecord.getRecordJson() != null) {
							JSONObject jsonRecord = new JSONObject(specRecord.getRecordJson());
							data += "The Phase: " + jsonRecord.getString("phase") + ":  ";
							if (jsonRecord.getBoolean("isStarting")) {
								data += "was STARTED\n";
							} else {
								data += "has ENDED\n";
							}
						}
					} catch (Exception e) {
						log.error("Error creating the RolePlayerMessage note!", e);
					}
					data += "endrnote\n";
				} else if (record.getRecordType().equalsIgnoreCase("SessionMgmt")) {
					data += "==";
					try {
						Record specRecord = recordRepo.findObjectById(record.getId());
						if (specRecord.getRecordJson() != null) {
							JSONObject jsonRecord = new JSONObject(specRecord.getRecordJson());
							data += jsonRecord.getString("sessionName") + ": " + jsonRecord.getString("sessionState");
						}
					} catch (Exception e) {
						log.error("Error creating the SessionMgmt note!", e);
					}
					data += "==\n";
				} else if (record.getRecordType().equalsIgnoreCase("RequestStartInject")) {
					if (participants.indexOf(sender) < 0) {
						participants += "participant \"" + sender + "\" as " + sender + " #Snow|Tan\n";
					}
					
					data += "rnote over " + sender + " #Aqua\n";
					try {
						Record specRecord = recordRepo.findObjectById(record.getId());
						if (specRecord.getRecordJson() != null) {
							JSONObject jsonRecord = new JSONObject(specRecord.getRecordJson());
						}
					} catch (Exception e) {
						log.error("Error creating the StartInject note!", e);
					}
					data += "endrnote\n";
				} else if (record.getRecordType().equalsIgnoreCase("Post")) {
					if (participants.indexOf(sender) < 0) {
						participants += "participant \"" + sender + "\" as " + sender + " #Snow|Tan\n";
					}
					
					data += "rnote over " + sender + " #AquaMarine\n";
					try {
						Record specRecord = recordRepo.findObjectById(record.getId());
						if (specRecord.getRecordJson() != null) {
							JSONObject jsonRecord = new JSONObject(specRecord.getRecordJson());
						}
					} catch (Exception e) {
						log.error("Error creating the Post Message note!", e);
					}
					data += "endrnote\n";
				} else {
					
					if (participants.indexOf(sender) < 0) {
						participants += "participant \"" + sender + "\" as " + sender + " #Snow|Tan\n";
					}
					String topic = record.getTopic();
					String msg = this.getMessageFromRecord(record);
					data += "group " + sender + " - " + record.getRecordType() + "\n"; 
					data += sender + " -[#green]-> " + cis + " : [[/details.html?recordId=" + record.getId() + " " + msg + "]]" + "\n";
					data += "activate " + sender + "\n";
					List<TopicReceiver> topicReceiver = receiverMap.get(topic);
					if (topicReceiver != null) {
						for (TopicReceiver receiver : topicReceiver) {
							if (!receiver.getClientId().equals(record.getClientId())) {
								String client = receiver.getClientId();
								idx = client.indexOf("--");
								if (idx > -1) {
									client = client.substring(0, idx);
								}
								client = client.replaceAll(" ", "_");
								client = client.replaceAll("-", "_");
								client = client.replaceAll("\\.", "_");
								client = client.replaceAll(":", "_");
								data += cis + " -[#blue]-> " + client/*  + " : " + msg*/ + "\n";
							}
						}
					}
					data += "deactivate  " + sender + "\n";;
					data += "end\n";
				}
			} else if (record.getRecordType().equalsIgnoreCase("LOG") && record.getMsgType().equalsIgnoreCase("EVAL")) {
				data += "rnote over " + sender + " #DimGray\n";
				try {
					Record specRecord = recordRepo.findObjectById(record.getId());
					if (specRecord.getRecordJson() != null) {
						JSONObject jsonRecord = new JSONObject(specRecord.getRecordJson());
						data += jsonRecord.getString("log") + "\n";
					}
				} catch (Exception e) {
					log.error("Error creating the RolePlayerMessage note!", e);
				}
				data += "endrnote\n";
			}
		}
		
		data = data.replace("{participants}", participants);
		data += "@enduml\n";
		log.info("createSequenceDiagramString-->");
		return data;
	}
	
	private String getMessageFromRecord(Record record) {
		String msg = "";
		msg += record.getCreateDate() + " - " + record.getRecordType();
		
		if (record.getHeadline() != null) {
			msg += ": " + record.getHeadline();
		}
		
		return msg;
	}
	
	private String createDiagramStart() {
		String data = "@startuml\n" +
		"'skinparam ParticipantPadding 20\n" +
		"skinparam defaultTextAlignment center\n" +
		"skinparam sequenceMessageAlign center\n" +
		"skinparam noteTextAlign center\n" +
		"skinparam shadowing false\n" +
		"skinparam sequenceLifeLineBorderColor black\n" +
		"skinparam sequenceLifeLineBorderThickness 0.5\n" +
		"skinparam sequenceBoxBorderColor black\n" +
		"skinparam sequenceLifeLineBackgroundColor AntiqueWhite\n" +
		"skinparam ActivityBorderThickness 0.5\n" +
		"skinparam SequenceDividerBorderThickness 0.5\n" +
		"skinparam note {\n" +
		"BackgroundColor lightyellow\n" +
		"BorderColor black\n" +
		"'FontSize 6\n" +
		"FontSize 10\n" +
		"BorderThickness 0.5\n" +
		"}\n" +
		"skinparam sequenceArrow {\n" +
		"Thickness 0.5\n" +
		"'FontSize 6\n" +
		"FontSize 10\n" +
		"Color black\n" +
		"}\n" +
		"skinparam sequenceParticipant {\n" +
		"BorderColor black\n" +
		"BorderThickness 0.5\n" +
		"FontStyle plain\n" +
		"'FontSize 6\n" +
		"FontSize 10\n" +
		"}\n" +
		"skinparam sequenceDivider {\n" +
		"BorderColor black\n" +
		"BorderThickness 0.5\n" +
		"FontStyle plain\n" +
		"'FontSize 6\n" +
		"FontSize 10\n" +
		"}\n" +
		"skinparam sequenceGroup {\n" +
		"'HeaderFontSize 6\n" +
		"HeaderFontSize 10\n" +
		"BorderThickness 0.5\n" +
		"HeaderFontStyle plain\n" +
		"BackgroundColor white\n" +
		"'FontSize 6\n" +
		"FontSize 10\n" +
		"}\n" +
		"hide footbox\n" +
		"||15|||\n";
		
		return data;
	}
	
	private void pack(String sourceDirPath, String zipFilePath) throws IOException {
	    Path p = Files.createFile(Paths.get(zipFilePath));
	    try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
	        Path pp = Paths.get(sourceDirPath);
	        Files.walk(pp)
	          .filter(path -> !Files.isDirectory(path))
	          .forEach(path -> {
	              ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
	              try {
	                  zs.putNextEntry(zipEntry);
	                  Files.copy(path, zs);
	                  zs.closeEntry();
	            } catch (IOException e) {
	                System.err.println(e);
	            }
	          });
	    }
	}
	
	private void unzip(final String zipFilePath, final String unzipLocation) throws IOException {

        if (!(Files.exists(Paths.get(unzipLocation)))) {
            Files.createDirectories(Paths.get(unzipLocation));
        }
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                Path filePath = Paths.get(unzipLocation, entry.getName());
                if (!entry.isDirectory()) {
                	// create the DIR structure
                	String strPath = filePath.toString();
                	int idx = strPath.lastIndexOf("\\");
                	if (idx < 0) {
                		idx = strPath.lastIndexOf("/");
                	}
                	Files.createDirectories(Paths.get(strPath.substring(0, idx)));
                	File file = new File(strPath);
                	file.createNewFile();
                	//Files.createFile(filePath);
                    unzipFiles(zipInputStream, filePath);
                } else {
                    Files.createDirectories(filePath);
                }

                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }
        }
    }

	private void unzipFiles(final ZipInputStream zipInputStream, final Path unzipFilePath) throws IOException {

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(unzipFilePath.toAbsolutePath().toString(), false))) {
            byte[] bytesIn = new byte[1024];
            int read = 0;
            while ((read = zipInputStream.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }

    }

}
