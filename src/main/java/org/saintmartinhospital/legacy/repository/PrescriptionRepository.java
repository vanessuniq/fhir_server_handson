package org.saintmartinhospital.legacy.repository;

import java.util.List;
import org.saintmartinhospital.legacy.domain.Prescription;
import org.saintmartinhospital.legacy.domain.PrescriptionStateEnum;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionRepository extends CrudRepository<Prescription,Integer> {
	
	@Query( "select p from Prescription p where p.person.id = :personId and p.state = :state order by createDate" )
	List<Prescription> findByPersonState( @Param("personId") Integer personaId, @Param("state") PrescriptionStateEnum state );
	
	@Query( "select p from Prescription p where p.person.id = :personId order by createDate" )
	List<Prescription> findByPerson( @Param("personId") Integer personId );
	
}
