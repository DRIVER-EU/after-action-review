package eu.driver.aar.service.dto.record;

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

/**
 * The persistent class for the record database table.
 * 
 */
@Entity
@Table(name="record", schema = "aar_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="Record.findAll", query="SELECT u FROM Record u")
public class Record {
	
	@Id
	@SequenceGenerator(sequenceName = "admin_service.record_seq", name = "RecordIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RecordIdSequence")
	@Column(unique=true, nullable=false)
	private Long id;
	
	@Column(name="clientId", length=25)
	private String clientId = null;
	
	@Column(name="createDate")
	private Date createDate;
	
	@Column(name="recordType")
	private String recordType;
	
	@Column(name="recordJson")
	private String recordJson;
	
	@Column(name="recordData")
	private String recordData;
	
	public Record() {
		
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
}
