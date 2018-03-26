package eu.driver.aar.service.ws.object;

public class WSTopicCreationNotification {
	
	private String mutation = "UPDATE_TOPIC";
	private String id = null;
	private Boolean state = false;
	
	public WSTopicCreationNotification() {
		
	}
	
	public WSTopicCreationNotification(String id, Boolean state) {
		this.id = id;
		this.state = state;
	}
	
	public String getMutation() {
		return mutation;
	}

	public void setMutation(String mutation) {
		this.mutation = mutation;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean getState() {
		return state;
	}

	public void setState(Boolean state) {
		this.state = state;
	}

}
