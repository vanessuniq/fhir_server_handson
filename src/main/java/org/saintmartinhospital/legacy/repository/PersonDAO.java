package org.saintmartinhospital.legacy.repository;

import java.util.List;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.legacy.service.bo.FindPersonByCriteriaBO;


public interface PersonDAO {

	Person attach( Person person );
	Person findById( Integer id );
	Person save( Person person );
	List<Person> findByCriteria( FindPersonByCriteriaBO criteria );
	List<Person> findByDocument( String typeAbrev, String docValue );		
	
}
