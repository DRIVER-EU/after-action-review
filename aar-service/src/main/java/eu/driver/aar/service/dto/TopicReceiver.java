package eu.driver.aar.service.dto;

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

@Entity
@Table(name="topicreceiver", schema = "aar_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="TopicReceiver.findAll", query="SELECT u FROM TopicReceiver u")
public class TopicReceiver {
	
	@Id
	@SequenceGenerator(sequenceName = "aar_service.topicreceiver_seq", name = "TopicReceiverIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TopicReceiverIdSequence")
	@Column(unique=true, nullable=false)
	private Long Id;
	
	@Column(name="trialId")
	private String trialId;
	
	@Column(name="topicName")
	private String topicName;
	
	@Column(name="clientId")
	private String clientId;
	
	public TopicReceiver() {
		
	}

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}

	public String getTrialId() {
		return trialId;
	}

	public void setTrialId(String trialId) {
		this.trialId = trialId;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String createBackupString(String backupType) {
    	StringBuffer backupBuffer = new StringBuffer();
    	
    	if (backupType.equalsIgnoreCase(AARConstants.BACKUP_TYPE_CSV)) {
    		// create the CSV strings
    		backupBuffer.append("\"").append(this.Id).append("\"").append(";");
    		backupBuffer.append("\"").append(this.trialId).append("\"").append(";");
    		backupBuffer.append("\"").append(this.topicName).append("\"").append(";");
    		backupBuffer.append("\"").append(this.clientId).append("\"").append("\n");
    		
    	} else if (backupType.equalsIgnoreCase(AARConstants.BACKUP_TYPE_SQL)) {
    		// create the SQL insert commands
    		backupBuffer.append("insert into aar_service.topicreceiver (Id, trialId, topicName, clientId) values (");
    		backupBuffer.append(this.Id).append(",");
    		backupBuffer.append("'").append(this.trialId).append("'").append(",");
    		backupBuffer.append("'").append(this.topicName).append("'").append(",");
    		backupBuffer.append("'").append(this.clientId).append("'").append(");").append("\n");
    	}
    	
    	return backupBuffer.toString();
    }
}
