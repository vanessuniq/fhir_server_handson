package org.saintmartinhospital.business.repository;

import java.util.List;
import org.saintmartinhospital.business.domain.Person;
import org.saintmartinhospital.business.service.bo.FindPersonByCriteriaBO;

/**
 *
 * @author julian.martinez
 */
public interface PersonDAO {

	Person attach( Person person );
	Person findById( Integer id );
	Person save( Person person );
	List<Person> findByCriteria( FindPersonByCriteriaBO criteria );	
	
}
