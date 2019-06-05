package org.saintmartinhospital.business.repository;

import org.saintmartinhospital.business.domain.DocType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocTypeRepository extends CrudRepository<DocType,Short> {
	
	@Query( "select t from DocType t where abrev = :abrev" )
	DocType findByAbrev( String abrev );
	
}
