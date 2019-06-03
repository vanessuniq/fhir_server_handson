package org.saintmartinhospital.fhir.service.patient.converter.mappings.impl;

import java.util.Calendar;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.Patient;
import org.saintmartinhospital.business.domain.Person;
import org.springframework.stereotype.Component;
import org.saintmartinhospital.fhir.service.patient.converter.mappings.PatientBirthDateMapping;


@Component
public class PatientBirthDateMappingImpl implements PatientBirthDateMapping {

	@Override
	public void mapTo( Patient patient, Person person ) {
		Validate.notNull( patient, "Unexpecte null patient" );
		Validate.notNull( person, "Unexpected null person" );
		addBirthdate( patient, person.getBirthDate() );	
	}
	
	@Override
	public void mapFrom( Patient patient, Person person ) {
		Validate.notNull( patient, "Unexpecte null patient" );
		Validate.notNull( person, "Unexpected null person" );
		 
		if( patient.getBirthDate() != null ) {
			Calendar fechaNac = Calendar.getInstance();
			fechaNac.setTime( patient.getBirthDate() );
			person.setBirthDate( fechaNac );
		}
	}
	

/*
 * private methods	
 */	
	
	private void addBirthdate( Patient patient, Calendar birthdate ) {
		if( birthdate != null )
			patient.setBirthDate( birthdate.getTime() );
	}

}
