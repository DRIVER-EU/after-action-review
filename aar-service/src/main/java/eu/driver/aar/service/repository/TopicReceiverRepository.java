package eu.driver.aar.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.aar.service.dto.TopicReceiver;

@RepositoryRestResource
public interface TopicReceiverRepository extends JpaRepository<TopicReceiver, Long> {
	
	public final static String ID_QUERY = "SELECT u FROM TopicReceiver u where u.id=:objectId";
	
	public final static String TRIAL_ID_QUERY = "SELECT u FROM TopicReceiver u where u.trialId=:trialId";
	
	public final static String CLIENT_ID_QUERY = "SELECT u FROM TopicReceiver u where u.clientId=:clientId";
	
	public final static String TOPIC_NAME_QUERY = "SELECT u FROM TopicReceiver u where u.topicName=:topicName";
	
	public final static String TRIAL_CLIENT_TOPIC_QUERY = "SELECT u FROM TopicReceiver u where u.trialId=:trialId AND u.clientId=:clientId AND u.topicName=:topicName";
	
	@Query(ID_QUERY)
    public TopicReceiver findObjectById(@Param("objectId") Long objectId);
	
	@Query(TRIAL_ID_QUERY)
    public List<TopicReceiver> findObjectsByTrialId(@Param("trialId") String trialId);
	
	@Query(CLIENT_ID_QUERY)
    public List<TopicReceiver> findObjectsByClientId(@Param("clientId") String clientId);
	
	@Query(TOPIC_NAME_QUERY)
    public List<TopicReceiver> findObjectsByTopicName(@Param("topicName") String topicName);
	
	@Query(TRIAL_CLIENT_TOPIC_QUERY)
    public TopicReceiver findObjectByTrialClientTopic(
    		@Param("trialId") String trialId,
    		@Param("clientId") String clientId,
    		@Param("topicName") String topicName);
	
	
}