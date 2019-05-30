package org.saintmartinhospital.business.repository;

import org.saintmartinhospital.business.domain.PersonDoc;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonDocRepository extends CrudRepository<PersonDoc,Integer> {
}
