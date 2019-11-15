package eu.driver.aar.service.ws.object;

public class WSMapLayerUpdate {
	private String mutation = "UPDATE_MAP_LAYER";
	private String dataJson = null;
	
	public WSMapLayerUpdate() {
		
	}
	
	public WSMapLayerUpdate(String dataJson) {
	   this.dataJson = dataJson;
	}

	public String getMutation() {
		return mutation;
	}

	public void setMutation(String mutation) {
		this.mutation = mutation;
	}

	public String getDataJson() {
		return dataJson;
	}

	public void setDataJson(String dataJson) {
		this.dataJson = dataJson;
	}
}
