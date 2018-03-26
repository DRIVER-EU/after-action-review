package eu.driver.aar.service.ws.object;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class WSHeartbeatRequest {
	private String requestId;
	private String mutation = "HBREQUEST";
	//@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private Date sendTime;
	
	public WSHeartbeatRequest() {
		
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
	
	public WSHeartbeatResponse createResponse() {
		WSHeartbeatResponse response = new WSHeartbeatResponse();
		
		response.setRequestId(this.requestId);
		response.setSendTime(new Date());
		response.setState("OK");
		
		return response;
	}
}
