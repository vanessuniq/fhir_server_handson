package org.saintmartinhospital.business.service.impl;

import java.util.Calendar;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.saintmartinhospital.business.domain.Person;
import org.saintmartinhospital.business.service.PersonService;
import org.saintmartinhospital.business.repository.PersonDAO;
import org.saintmartinhospital.business.service.bo.FindPersonByCriteriaBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PersonServiceImpl implements PersonService {
	
	@Autowired
	private PersonDAO personDAO;
	@Autowired
	private PersonDocServiceImpl personDocService;

	@Transactional
	@Override
	public Person findById( Integer id ) {
		return personDAO.findById( id );
	}

	@Transactional
	@Override
	public List<Person> findByCriteria( FindPersonByCriteriaBO criteria ) {
		return personDAO.findByCriteria( criteria );
	}

	@Override
	public Person attach( Person person ) {
		return personDAO.attach( person );
	}

	@Transactional
	@Override
	public Person save( Person person ) {
		if( person != null ) {
			person.setCreateDate( Calendar.getInstance() );
			personDAO.save( person );
			CollectionUtils.emptyIfNull( person.getDocs() ).forEach( personDoc -> {
				personDoc.setPerson( person );
				personDocService.save( personDoc );
			});
		}
		return person;
	}

}
