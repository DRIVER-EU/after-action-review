package eu.driver.aar.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.aar.service.dto.Szenario;

@RepositoryRestResource
public interface SzenarioRepository extends JpaRepository<Szenario, Long> {
	
	public final static String ID_QUERY = "SELECT u FROM Szenario u where u.id=:objectId";
	
	public final static String SZENARIO_ID_QUERY = "SELECT u FROM Szenario u where u.szenarioId=:objectId";
	
	@Query(ID_QUERY)
    public Szenario findObjectById(@Param("objectId") Long objectId);
	
	@Query(SZENARIO_ID_QUERY)
    public Szenario findObjectBySzenarioId(@Param("objectId") String objectId);
}