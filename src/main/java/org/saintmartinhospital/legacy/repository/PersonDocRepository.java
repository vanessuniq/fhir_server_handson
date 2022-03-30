package org.saintmartinhospital.legacy.repository;

import org.saintmartinhospital.legacy.domain.PersonDoc;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonDocRepository extends CrudRepository<PersonDoc,Integer> {
}
