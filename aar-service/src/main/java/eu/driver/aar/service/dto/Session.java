package eu.driver.aar.service.dto;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import eu.driver.aar.service.constants.AARConstants;


@Entity
@Table(name="session", schema = "aar_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="Session.findAll", query="SELECT u FROM Session u")
public class Session {
	
	@Id
	@SequenceGenerator(sequenceName = "aar_service.session_seq", name = "SessionIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SessionIdSequence")
	@Column(unique=true, nullable=false)
	private Long id;
	
	@Column(name="sessionId")
	private String sessionId;
	
	@Column(name="sessionName")
	private String sessionName;
	
	@Column(name="startDate")
	private Date startDate;
	
	@Column(name="endDate")
	private Date endDate;
	
	@JsonBackReference
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "szenario_id")
	private Szenario szenario;
	
	public Session() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionName() {
		return sessionName;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Szenario getSzenario() {
		return szenario;
	}

	public void setSzenario(Szenario szenario) {
		this.szenario = szenario;
	}
	
	public String createBackupString(String backupType) {
    	StringBuffer backupBuffer = new StringBuffer();
    	
    	if (backupType.equalsIgnoreCase(AARConstants.BACKUP_TYPE_CSV)) {
    		// create the CSV strings
    		backupBuffer.append("\"").append(this.id).append("\"").append(";");
    		backupBuffer.append("\"").append(this.sessionId).append("\"").append(";");
    		backupBuffer.append("\"").append(this.sessionName).append("\"").append(";");
    		backupBuffer.append("\"").append(this.startDate).append("\"").append(";");
    		backupBuffer.append("\"").append(this.endDate).append("\"").append(";");
    		backupBuffer.append("\"").append(this.szenario.getId()).append("\"").append("\n");
    		
    	} else if (backupType.equalsIgnoreCase(AARConstants.BACKUP_TYPE_SQL)) {
    		// create the SQL insert commands
    		backupBuffer.append("insert into aar_service.session (id, sessionId, sessionName, startDate, endDate, szenario_id) values (");
    		
    		backupBuffer.append(this.id).append(",");
    		backupBuffer.append("'").append(this.sessionId).append("'").append(",");
    		backupBuffer.append("'").append(this.sessionName).append("'").append(",");
    		backupBuffer.append("'").append(this.startDate).append("'").append(",");
    		backupBuffer.append("'").append(this.endDate).append("'").append(",");
    		backupBuffer.append(this.szenario.getId()).append(");").append("\n");
    	}
    	
    	return backupBuffer.toString();
    }
}
