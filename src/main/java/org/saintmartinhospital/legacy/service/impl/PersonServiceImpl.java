package org.saintmartinhospital.legacy.service.impl;

import java.util.Calendar;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.legacy.service.PersonService;
import org.saintmartinhospital.legacy.repository.PersonDAO;
import org.saintmartinhospital.legacy.service.PersonDocService;
import org.saintmartinhospital.legacy.service.bo.FindPersonByCriteriaBO;
import org.saintmartinhospital.legacy.service.exceptions.PersonNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PersonServiceImpl implements PersonService {

	@Autowired
	private PersonDAO personDAO;
	@Autowired
	private PersonDocService personDocService;

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
	public Person attach(Person person) {
		return personDAO.attach(person);
	}

	@Transactional
	@Override
	public Person save( Person person ) {
		if( person != null ) {
			person.setCreateDate( Calendar.getInstance() );
			personDAO.save( person );
			CollectionUtils.emptyIfNull( person.getDocs()).forEach( personDoc -> {
				personDoc.setPerson( person );
				personDocService.save( personDoc );
			});
		}
		return person;
	}

	@Transactional
	@Override
	public Person update( Person person ) throws PersonNotFoundException {
		Person personToReturn = null;
		if( person != null ) {
			Person savedPerson = findById( person.getId() );
			if( savedPerson == null )
				throw new PersonNotFoundException();
			
			BeanUtils.copyProperties( person, savedPerson, "id", "docs", "createDate" );
			savedPerson.setDocs( personDocService.updateDocs( savedPerson, person.getDocs() ) );
			personToReturn = personDAO.save( savedPerson );
		}
		return personToReturn;
	}

}
