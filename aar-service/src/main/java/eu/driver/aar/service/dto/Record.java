package eu.driver.aar.service.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import eu.driver.aar.service.constants.AARConstants;

/**
 * The persistent class for the record database table.
 * 
 */
@Entity
@Table(name="record", schema = "aar_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="Record.findAll", query="SELECT u FROM Record u")
public class Record {
	
	@Id
	@SequenceGenerator(sequenceName = "aar_service.record_seq", name = "RecordIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RecordIdSequence")
	@Column(unique=true, nullable=false)
	private Long id;
	
	@Column(name="clientId", length=255)
	private String clientId = null;
	
	@Column(name="sessionId")
	private String sessionId;
	
	@Column(name="createDate")
	private Date createDate;
	
	@Column(name="trialDate")
	private Date trialDate;
	
	@Column(name="topic")
	private String topic;
	
	@Column(name="recordType")
	private String recordType;
	
	@Column(name="recordJson", columnDefinition = "TEXT")
	private String recordJson;
	
	@Column(name="recordData", columnDefinition = "TEXT")
	private String recordData;
	
	@Column(name="headline", columnDefinition = "TEXT")
	private String headline;
	
	@Column(name="msgType")
	private String msgType;
	
	@Column(name="runType", length=256)
	private String runType = AARConstants.RECORD_RUN_TYPE_IN;
	
	@JsonManagedReference
	@OneToMany( mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonBackReference
	private List<Attachment> attachments = new ArrayList<Attachment>();
	
	public Record() {
		
	}
	
	public Record(Long id) {
		this.id = id;
	}
	
	public Record(Long id, String clientId, String sessionId, String topic, String recordType, Date createDate, Date trialDate, String headline, String msgType, String runType) {
		this.id = id;
		this.clientId = clientId;
		this.sessionId = sessionId;
		this.createDate = createDate;
		this.trialDate = trialDate;
		this.topic = topic;
		this.recordType = recordType;
		this.headline = headline;
		this.msgType = msgType;
		this.runType = runType;
		
	}
	
	public Record(Long id, String clientId, String topic, String recordType, Date createDate, String headline, String msgType) {
		this.id = id;
		this.clientId = clientId;
		this.topic = topic;
		this.recordType = recordType;
		this.createDate = createDate;
		this.headline = headline;
		this.msgType = msgType;
	}
	
	public Record(Long id, String topic, String recordType, Date createDate, String headline, String msgType) {
		this.id = id;
		this.topic = topic;
		this.recordType = recordType;
		this.createDate = createDate;
		this.headline = headline;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getTrialDate() {
		return trialDate;
	}

	public void setTrialDate(Date trialDate) {
		this.trialDate = trialDate;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public String getRecordJson() {
		return recordJson;
	}

	public void setRecordJson(String recordJson) {
		this.recordJson = recordJson;
	}

	public String getRecordData() {
		return recordData;
	}

	public void setRecordData(String recordData) {
		this.recordData = recordData;
	}
	
	public String getHeadline() {
		return headline;
	}

	public void setHeadline(String headline) {
		this.headline = headline;
	}
	
	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getRunType() {
		return runType;
	}

	public void setRunType(String runType) {
		this.runType = runType;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}
	
	public void addAttachment(Attachment file) {
		this.attachments.add(file);
	}

	public String createBackupString(String backupType) {
    	StringBuffer backupBuffer = new StringBuffer();
    	
    	if (backupType.equalsIgnoreCase(AARConstants.BACKUP_TYPE_CSV)) {
    		// create the CSV strings
    		backupBuffer.append("\"").append(this.id).append("\"").append(";");
    		backupBuffer.append("\"").append(this.clientId).append("\"").append(";");
    		backupBuffer.append("\"").append(this.sessionId).append("\"").append(";");
    		backupBuffer.append("\"").append(this.createDate).append("\"").append(";");
    		backupBuffer.append("\"").append(this.trialDate).append("\"").append(";");
    		backupBuffer.append("\"").append(this.topic).append("\"").append(";");
    		backupBuffer.append("\"").append(this.recordType).append("\"").append(";");
    		backupBuffer.append("\"").append(this.headline).append("\"").append(";");
    		backupBuffer.append("\"").append(this.msgType).append("\"").append(";");
    		backupBuffer.append("\"").append(this.recordJson).append("\"").append(";");
    		backupBuffer.append("\"").append(this.runType).append("\"").append(";");
    		backupBuffer.append("\"").append(this.recordData).append("\"").append("\n");
    		
    		for (Attachment attachment : this.attachments) {
    			backupBuffer.append(attachment.createBackupString(backupType));
    		}
    		
    	} else if (backupType.equalsIgnoreCase(AARConstants.BACKUP_TYPE_SQL)) {
    		// create the SQL insert commands
    		backupBuffer.append("insert into aar_service.record (id, clientId, sessionId, createDate, trialDate, topic, recordType, headline, msgType, recordJson, recordData, runType) values (");
    		
    		backupBuffer.append(this.id).append(",");
    		backupBuffer.append("'").append(this.clientId).append("'").append(",");
    		backupBuffer.append("'").append(this.sessionId).append("'").append(",");
    		backupBuffer.append("'").append(this.createDate).append("'").append(",");
    		backupBuffer.append("'").append(this.trialDate).append("'").append(",");
    		backupBuffer.append("'").append(this.topic).append("'").append(",");
    		backupBuffer.append("'").append(this.recordType).append("'").append(",");
    		backupBuffer.append("'").append(this.headline).append("'").append(",");
    		backupBuffer.append("'").append(this.msgType).append("'").append(",");
    		String json = this.recordJson.replaceAll("'", "\\''");
    		backupBuffer.append("'").append(json).append("'").append(",");
    		backupBuffer.append("'").append(this.recordData).append("'").append(",");
    		backupBuffer.append("'").append(this.runType).append("'").append(");").append("\n");
    		
    		for (Attachment attachment : this.attachments) {
    			backupBuffer.append(attachment.createBackupString(backupType));
    		}
    	}
    	
    	return backupBuffer.toString();
    }
}
