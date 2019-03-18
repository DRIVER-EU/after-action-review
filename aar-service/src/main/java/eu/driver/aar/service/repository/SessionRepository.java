package eu.driver.aar.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.aar.service.dto.Session;

@RepositoryRestResource
public interface SessionRepository extends JpaRepository<Session, Long> {
	
	public final static String ID_QUERY = "SELECT u FROM Session u where u.id=:objectId";
	
	public final static String SESSION_ID_QUERY = "SELECT u FROM Session u where u.sessionId=:objectId";
	
	@Query(ID_QUERY)
    public Session findObjectById(@Param("objectId") Long objectId);
	
	@Query(SESSION_ID_QUERY)
    public Session findObjectBySessionId(@Param("objectId") String objectId);
}