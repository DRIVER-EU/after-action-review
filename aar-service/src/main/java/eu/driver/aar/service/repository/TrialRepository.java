package eu.driver.aar.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.aar.service.dto.Trial;

@RepositoryRestResource
public interface TrialRepository extends JpaRepository<Trial, Long> {
	
	public final static String ID_QUERY = "SELECT u FROM Trial u where u.id=:objectId";
	
	public final static String TRIAL_ID_QUERY = "SELECT u FROM Trial u where u.trialId=:objectId";
	
	public final static String ACTUAL_TRIAL_QUERY = "SELECT u FROM Trial u where u.actual=true";
	
	@Query(ID_QUERY)
    public Trial findObjectById(@Param("objectId") Long objectId);
	
	@Query(TRIAL_ID_QUERY)
    public Trial findObjectByTrialId(@Param("objectId") String objectId);
	
	@Query(ACTUAL_TRIAL_QUERY)
    public Trial findActualTrial();
	
	
}