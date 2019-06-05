package org.saintmartinhospital.business.service;

import java.util.List;
import org.saintmartinhospital.business.domain.Person;
import org.saintmartinhospital.business.service.bo.FindPersonByCriteriaBO;

public interface PersonService {
	
	Person findById( Integer id );
	List<Person> findByCriteria( FindPersonByCriteriaBO criteria );
	Person attach( Person person );
	
}
