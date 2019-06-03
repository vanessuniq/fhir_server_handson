package org.saintmartinhospital.fhir.service.patient.converter.mappings.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Patient;
import org.saintmartinhospital.business.domain.Person;
import org.saintmartinhospital.fhir.service.patient.converter.mappings.PatientEmailMapping;
import org.springframework.stereotype.Component;


@Component
public class PatientEmailMappingImpl implements PatientEmailMapping {

	@Override
	public void mapTo( Patient patient, Person person ) {
		Validate.notNull( patient, "Unexpected null patient" );
		Validate.notNull( person, "Unexpected null person" );

		if( person != null )
			addEmail( patient, person.getEmail() );
	}
	
	@Override
	public void mapFrom( Patient patient, Person person ) {
		Validate.notNull( patient, "Unexpected null patient" );
		Validate.notNull( person, "Unexpected null person" );
		
		Optional<ContactPoint> optional = CollectionUtils.emptyIfNull( patient.getTelecom() ).stream()
			.filter( contactPoint -> ContactPoint.ContactPointSystem.EMAIL.equals( contactPoint.getSystem() ) ).findFirst();
		
		if( !Optional.empty().equals( optional ) )
			person.setEmail( optional.get().getValue() );
	}
	

/*
 * private methods	
 */	
	
	private void addEmail( Patient patient, String numTelefono ) {
		if( StringUtils.isNotEmpty( numTelefono ) ) {
			ContactPoint contact = new ContactPoint();
			contact.setSystem( ContactPoint.ContactPointSystem.EMAIL );
			contact.setValue( numTelefono );
			patient.setTelecom( Collections.singletonList( contact ) );		
		}		
	}	
	
}
