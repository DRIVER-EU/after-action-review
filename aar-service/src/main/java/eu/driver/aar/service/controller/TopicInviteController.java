package eu.driver.aar.service.controller;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.adapter.core.CISAdapter;
import eu.driver.api.IAdaptorCallback;

@RestController
public class TopicInviteController implements IAdaptorCallback {
	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	RecordRESTController recordCallback;

	public TopicInviteController() {
		log.info("TopicInviteController");
	}
	
	@Override
	public void messageReceived(IndexedRecord key, IndexedRecord receivedMessage) {
		
		eu.driver.model.core.TopicInvite inviteMsg = (eu.driver.model.core.TopicInvite) SpecificData.get().deepCopy(eu.driver.model.core.TopicInvite.SCHEMA$, receivedMessage);
		if (inviteMsg.getId().toString().equalsIgnoreCase(CISAdapter.getInstance().getClientID()) && inviteMsg.getSubscribeAllowed()) {
			CISAdapter.getInstance().addCallback(recordCallback, inviteMsg.getTopicName().toString());
		}
	}

	public RecordRESTController getRecordCallback() {
		return recordCallback;
	}

	public void setRecordCallback(RecordRESTController recordCallback) {
		this.recordCallback = recordCallback;
	}
}
