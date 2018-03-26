package eu.driver.aar.service.ws.object;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class WSLogNotification {
	private String mutation = "LOG_NOTIFICATION";
	private Long id = 0L;
	private String level = null;
	private String clientId = null;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private Date sendDate = null;
	private String message = null;

	public WSLogNotification(Long id, String level, String clientId, Date sendDate, String message) {
		this.id = id;
		this.level = level;
		this.clientId = clientId;
		this.sendDate = sendDate;
		this.message = message;

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

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
