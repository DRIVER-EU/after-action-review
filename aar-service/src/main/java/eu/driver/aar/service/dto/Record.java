package eu.driver.aar.service.dto;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
	
	public Record() {
		
	}
	
	public Record(Long id, String clientId, String sessionId, String topic, String recordType, Date createDate, Date trialDate) {
		this.id = id;
		this.clientId = clientId;
		this.sessionId = sessionId;
		this.createDate = createDate;
		this.trialDate = trialDate;
		this.topic = topic;
		this.recordType = recordType;
		
	}
	
	public Record(Long id, String clientId, String topic, String recordType, Date createDate) {
		this.id = id;
		this.clientId = clientId;
		this.topic = topic;
		this.recordType = recordType;
		this.createDate = createDate;
	}
	
	public Record(Long id, String topic, String recordType, Date createDate) {
		this.id = id;
		this.topic = topic;
		this.recordType = recordType;
		this.createDate = createDate;
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
    		backupBuffer.append("\"").append(this.recordJson).append("\"").append(";");
    		backupBuffer.append("\"").append(this.recordData).append("\"").append("\n");
    		
    	} else if (backupType.equalsIgnoreCase(AARConstants.BACKUP_TYPE_SQL)) {
    		// create the SQL insert commands
    		backupBuffer.append("inseret into trial (id, clientId, sessionId, createDate, trialDate, topic, recordType, recordJson, recordData) values (");
    		
    		backupBuffer.append(this.id).append(",");
    		backupBuffer.append("'").append(this.clientId).append("'").append(",");
    		backupBuffer.append("'").append(this.sessionId).append("'").append(",");
    		backupBuffer.append("'").append(this.createDate).append("'").append(",");
    		backupBuffer.append("'").append(this.trialDate).append("'").append(",");
    		backupBuffer.append("'").append(this.topic).append("'").append(",");
    		backupBuffer.append("'").append(this.recordType).append("'").append(",");
    		backupBuffer.append("'").append(this.recordJson).append("'").append(",");
    		backupBuffer.append("'").append(this.recordData).append("'").append(");").append("\n");
    	}
    	
    	return backupBuffer.toString();
    }
}
