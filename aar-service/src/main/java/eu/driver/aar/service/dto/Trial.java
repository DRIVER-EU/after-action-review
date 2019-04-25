package eu.driver.aar.service.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import eu.driver.aar.service.constants.AARConstants;


@Entity
@Table(name="trial", schema = "aar_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="Trial.findAll", query="SELECT u FROM Trial u")
public class Trial {
	
	@Id
	@SequenceGenerator(sequenceName = "aar_service.trial_seq", name = "TrialIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TrialIdSequence")
	@Column(unique=true, nullable=false)
	private Long id;
	
	@Column(name="trialId")
	private String trialId;
	
	@Column(name="trialName")
	private String trialName;
	
	@Column(name="startDate")
	private Date startDate;
	
	@Column(name="endDate")
	private Date endDate;
	
	@Column(name="actual")
	private Boolean actual;
	
	@JsonManagedReference
	@OneToMany( mappedBy = "trial", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Szenario> szenarioList = new ArrayList<Szenario>();
	
	public Trial() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTrialId() {
		return trialId;
	}

	public void setTrialId(String trialId) {
		this.trialId = trialId;
	}
	
	public String getTrialName() {
		return trialName;
	}

	public void setTrialName(String trialName) {
		this.trialName = trialName;
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

	public Boolean getActual() {
		return actual;
	}

	public void setActual(Boolean actual) {
		this.actual = actual;
	}

	public List<Szenario> getSzenarioList() {
		return szenarioList;
	}

	public void setSzenarioList(List<Szenario> szenarioList) {
		this.szenarioList = szenarioList;
	}
	
	public void addSzenario(Szenario szenario) {
		this.szenarioList.add(szenario);
		szenario.setTrial(this);
    }
 
    public void removeSzenario(Szenario szenario) {
    	this.szenarioList.remove(szenario);
        szenario.setTrial(null);
    }
    
    public String createBackupString(String backupType) {
    	StringBuffer backupBuffer = new StringBuffer();
    	
    	if (backupType.equalsIgnoreCase(AARConstants.BACKUP_TYPE_CSV)) {
    		// create the CSV strings
    		backupBuffer.append("\"").append(this.id).append("\"").append(";");
    		backupBuffer.append("\"").append(this.trialId).append("\"").append(";");
    		backupBuffer.append("\"").append(this.trialName).append("\"").append(";");
    		backupBuffer.append("\"").append(this.startDate).append("\"").append(";");
    		backupBuffer.append("\"").append(this.endDate).append("\"").append(";");
    		backupBuffer.append("\"").append(this.actual).append("\"").append("\n");
    		
    		for (Szenario szenario : this.szenarioList) {
    			backupBuffer.append(szenario.createBackupString(backupType));
    		}
    		
    	} else if (backupType.equalsIgnoreCase(AARConstants.BACKUP_TYPE_SQL)) {
    		// create the SQL insert commands
    		for (Szenario szenario : this.szenarioList) {
    			backupBuffer.append(szenario.createBackupString(backupType));
    		}
    		
    		backupBuffer.append("inseret into trial (id, trialId, trialName, startDate, endDate, actual) values (");
    		backupBuffer.append(this.id).append(",");
    		backupBuffer.append("'").append(this.trialId).append("'").append(",");
    		backupBuffer.append("'").append(this.trialName).append("'").append(",");
    		backupBuffer.append("'").append(this.startDate).append("'").append(",");
    		backupBuffer.append("'").append(this.endDate).append("'").append(",");
    		backupBuffer.append(this.actual).append(");").append("\n");
    	}
    	
    	return backupBuffer.toString();
    }
}
