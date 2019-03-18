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
	
	@Query(ID_QUERY)
    public Record findObjectById(@Param("objectId") Long objectId);
}