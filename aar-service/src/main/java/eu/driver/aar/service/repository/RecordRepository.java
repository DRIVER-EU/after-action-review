package eu.driver.aar.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.aar.service.dto.Record;

@RepositoryRestResource
public interface RecordRepository extends JpaRepository<Record, Long> {
	
	public final static String ID_QUERY = "SELECT u FROM Record u where u.id=:objectId";
	public final static String COUNT_WITHOUT_LOG = "SELECT COUNT(u) FROM Record u WHERE u.recordType != 'Log'";
	public final static String QUERY_ALL_BY_RECORD_TYPE = "SELECT u FROM Record u where u.recordType=:recordType";
	
	@Query(ID_QUERY)
    public Record findObjectById(@Param("objectId") Long objectId);
	
	@Query(COUNT_WITHOUT_LOG)
	public Long countRecordsWithoutLog();
	
	@Query(QUERY_ALL_BY_RECORD_TYPE)
    public List<Record> findObjectsByRecordType(@Param("recordType") String recordType);
}