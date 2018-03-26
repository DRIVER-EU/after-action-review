package eu.driver.aar.service.ws.object;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class WSHeartbeatResponse {
	private String requestId;
	private String mutation = "HBRESPONSE";
	//@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private Date sendTime;
	private String state;
	
	public WSHeartbeatResponse() {
		
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getMutation() {
		return mutation;
	}

	public void setMutation(String mutation) {
		this.mutation = mutation;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
