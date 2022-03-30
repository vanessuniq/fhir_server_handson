package org.saintmartinhospital.legacy.service;

import java.util.Set;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.legacy.domain.PersonDoc;


public interface PersonDocService {

	PersonDoc save( PersonDoc personDoc );
	Set<PersonDoc> updateDocs( Person person, Set<PersonDoc> newDocs );
	
}
