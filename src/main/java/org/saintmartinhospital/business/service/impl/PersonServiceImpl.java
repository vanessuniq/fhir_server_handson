package org.saintmartinhospital.business.service.impl;

import org.saintmartinhospital.business.domain.Person;
import org.saintmartinhospital.business.repository.PersonRepository;
import org.saintmartinhospital.business.service.PersonService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PersonServiceImpl implements PersonService {
	
	@Autowired
	private PersonRepository personRepo;

	@Transactional
	@Override
	public Person findById( Integer id ) {
		Optional<Person> optional = personRepo.findById( id );
		return Optional.empty().equals( optional )? null: optional.get();
	}

}
