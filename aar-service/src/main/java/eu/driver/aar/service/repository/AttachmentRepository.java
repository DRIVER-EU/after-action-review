package eu.driver.aar.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.aar.service.dto.Attachment;

@RepositoryRestResource
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
	
	public final static String ID_QUERY = "SELECT u FROM Attachment u where u.id=:objectId";
	public final static String COUNT_QUERY= "SELECT COUNT(u) FROM Attachment u";
	
	@Query(ID_QUERY)
    public Attachment findObjectById(@Param("objectId") Long objectId);
	
	@Query(COUNT_QUERY)
	public Long countAttachments();
}