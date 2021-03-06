package eu.driver.aar.service.dto;

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

import eu.driver.aar.service.constants.AARConstants;

@Entity
@Table(name="file", schema = "aar_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="File.findAll", query="SELECT u FROM Attachment u")
public class Attachment {
	
	@Id
	@SequenceGenerator(sequenceName = "aar_service.file_seq", name = "FileIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FileIdSequence")
	@Column(unique=true, nullable=false)
	private Long id;
	
	@Column(name="name", columnDefinition = "TEXT")
	private String name;
	
	@Column(name="mimeType", columnDefinition = "TEXT")
	private String mimeType;
	
	@Column(name="url", columnDefinition = "TEXT")
	private String url;

	
	@JsonBackReference	
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "record_id")
	private Record record;
	
	public Attachment() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Record getRecord() {
		return record;
	}

	public void setRecord(Record record) {
		this.record = record;
	}
	
	public String createBackupString(String backupType) {
    	StringBuffer backupBuffer = new StringBuffer();
    	
    	if (backupType.equalsIgnoreCase(AARConstants.BACKUP_TYPE_CSV)) {
    		backupBuffer.append("\"").append(this.id).append("\"").append(";");
    		backupBuffer.append("\"").append(this.name).append("\"").append(";");
    		backupBuffer.append("\"").append(this.mimeType).append("\"").append(";");
    		backupBuffer.append("\"").append(this.url).append("\"").append(";");
    		backupBuffer.append("\"").append(this.record.getId()).append("\"").append(";");
    		
    	} else if (backupType.equalsIgnoreCase(AARConstants.BACKUP_TYPE_SQL)) {
    		backupBuffer.append("insert into aar_service.file (id, name, mimeType, url, record_id) values (");
    		backupBuffer.append(this.id).append(",");
    		backupBuffer.append("'").append(this.name).append("'").append(",");
    		backupBuffer.append("'").append(this.mimeType).append("'").append(",");
    		backupBuffer.append("'").append(this.url).append("'").append(",");
    		backupBuffer.append(this.record.getId()).append(");").append("\n");
    	}
    	
    	return backupBuffer.toString();
    	
	}
	
}
