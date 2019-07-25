package eu.driver.aar.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.aar.service.dto.Record;

@RepositoryRestResource
public interface RecordRepository extends JpaRepository<Record, Long> {
	
	public final static String ID_QUERY = "SELECT u FROM Record u where u.id=:objectId";
	public final static String COUNT_WITHOUT_LOG = "SELECT COUNT(u) FROM Record u WHERE u.recordType != 'Log'";
	
	@Query(ID_QUERY)
    public Record findObjectById(@Param("objectId") Long objectId);
	
	@Query(COUNT_WITHOUT_LOG)
	public Long countRecordsWithoutLog();
}