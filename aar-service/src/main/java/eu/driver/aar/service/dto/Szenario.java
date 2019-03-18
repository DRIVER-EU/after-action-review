package eu.driver.aar.service.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;


@Entity
@Table(name="szenario", schema = "aar_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="Szenario.findAll", query="SELECT u FROM Szenario u")
public class Szenario {
	
	@Id
	@SequenceGenerator(sequenceName = "aar_service.szenario_seq", name = "SzenarioIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SzenarioIdSequence")
	@Column(unique=true, nullable=false)
	private Long id;
	
	@Column(name="szenarioId")
	private String szenarioId;
	
	@Column(name="szenarioName")
	private String szenarioName;
	
	@Column(name="startDate")
	private Date startDate;
	
	@Column(name="endDate")
	private Date endDate;
	
	@JsonBackReference	
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trial_id")
	private Trial trial;
	
	@JsonManagedReference
	@OneToMany( mappedBy = "szenario", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Session> sessionList = new ArrayList<Session>();
	
	public Szenario() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSzenarioId() {
		return szenarioId;
	}

	public void setSzenarioId(String szenarioId) {
		this.szenarioId = szenarioId;
	}

	public String getSzenarioName() {
		return szenarioName;
	}

	public void setSzenarioName(String szenarioName) {
		this.szenarioName = szenarioName;
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

	public Trial getTrial() {
		return trial;
	}

	public void setTrial(Trial trial) {
		this.trial = trial;
	}

	public List<Session> getSessionList() {
		return sessionList;
	}

	public void setSessionList(List<Session> sessionList) {
		this.sessionList = sessionList;
	}
	
	public void addSession(Session session) {
		this.sessionList.add(session);
		session.setSzenario(this);
    }
 
    public void removeSession(Session session) {
    	this.sessionList.remove(session);
    	session.setSzenario(null);
    }
}
