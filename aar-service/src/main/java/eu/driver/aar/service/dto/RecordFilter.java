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
	private String msgType;
	
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
	
	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public boolean isFilterEnabled() {
		boolean enabled = false;
		
		if (this.id != null) {
			enabled = true;
		} else if (this.fromDate != null) {
			enabled = true;
		} else if (this.toDate != null) {
			enabled = true;
		} else if (this.recordType != null) {
			enabled = true;
		} else if (this.topicName != null) {
			enabled = true;
		} else if (this.senderClientId != null) {
			enabled = true;
		} else if (this.receiverClientId != null) {
			enabled = true;
		} else if (this.msgType != null) {
			enabled = true;
		}
		
		return enabled;
	}
	
	public boolean meetsRecordFilter(Record record) {
		boolean enabled = true;
		
		if (this.id != null || this.id == record.getId()) {
			enabled = false;
		} else if (this.fromDate != null && this.fromDate.before(record.getCreateDate())) {
			enabled = false;
		} else if (this.toDate != null && this.toDate.after(record.getCreateDate())) {
			enabled = false;
		} else if (this.recordType != null && this.recordType != record.getRecordType()) {
			enabled = false;
		} else if (this.topicName != null && this.topicName != record.getTopic()) {
			enabled = false;
		} else if (this.senderClientId != null && this.senderClientId != record.getClientId()) {
			enabled = true;
		}
		
		return enabled; 
	}
}
