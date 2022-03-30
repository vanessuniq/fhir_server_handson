package org.saintmartinhospital.legacy.service;

import java.util.List;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.legacy.service.bo.FindPersonByCriteriaBO;
import org.saintmartinhospital.legacy.service.exceptions.PersonNotFoundException;

public interface PersonService {

	Person findById( Integer id );
	List<Person> findByCriteria( FindPersonByCriteriaBO criteria );
	Person save( Person person );
	Person update( Person person ) throws PersonNotFoundException;

	Person attach(Person person);

}
