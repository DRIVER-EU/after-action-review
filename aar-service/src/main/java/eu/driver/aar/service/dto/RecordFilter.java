package eu.driver.aar.service.dto;

import java.util.Date;

public class RecordFilter {

	private Long id;
	private Date fromDate;
	private Date toDate;
	private String recordType;
	private String topicName;
	private String senderClientId;
	private String receiverClientId;
	
	public RecordFilter() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getSenderClientId() {
		return senderClientId;
	}

	public void setSenderClientId(String senderClientId) {
		this.senderClientId = senderClientId;
	}

	public String getReceiverClientId() {
		return receiverClientId;
	}

	public void setReceiverClientId(String receiverClientId) {
		this.receiverClientId = receiverClientId;
	}
}
