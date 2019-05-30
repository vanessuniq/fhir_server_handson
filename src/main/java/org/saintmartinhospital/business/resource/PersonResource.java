package org.saintmartinhospital.business.resource;

import org.saintmartinhospital.business.domain.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.saintmartinhospital.business.service.PersonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

@RestController
public class PersonResource {
	
	@Autowired
	private PersonService personService;
	
	@Transactional
	@RequestMapping( "/person/{id}" )
	public ResponseEntity<Person> findById( @PathVariable Integer id ) {
		Person person = personService.findById( id );
		return new ResponseEntity( person, person == null? HttpStatus.NOT_FOUND: HttpStatus.OK );
	}

}
