package eu.driver.aar.service.ws.object;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class WSRecordNotification {
	private String mutation = "RECORD_NOTIFICATION";
	private Long id = 0L;
	private String clientId = null;
	private String topic = null;
	private String headline = null;
	private String msgType = null;
	private String runType = null;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
	private Date createDate = null;
	
	private String recordType = null;
	private String recordJson = null;
	private String recordData = null;

	public WSRecordNotification(Long id, String clientId, String topic, Date createDate, String recordType, String headline, String msgType, String runType, String recordJson, String recordData) {
		this.id = id;
		this.clientId = clientId;
		this.topic = topic;
		this.createDate = createDate;
		this.recordType = recordType;
		this.headline = headline;
		this.msgType = msgType;
		this.runType = runType;
		this.recordJson = recordJson;
		this.recordData = recordData;

	}

	public String getMutation() {
		return mutation;
	}

	public void setMutation(String mutation) {
		this.mutation = mutation;
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
	
	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
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
		
}
