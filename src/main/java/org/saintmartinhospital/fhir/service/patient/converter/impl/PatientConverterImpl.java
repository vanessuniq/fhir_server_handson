package org.saintmartinhospital.fhir.service.patient.converter.impl;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.hl7.fhir.r4.model.Patient;
import org.saintmartinhospital.business.domain.Person;
import org.saintmartinhospital.business.service.PersonService;
import org.saintmartinhospital.fhir.common.converter.AbstractFhirResourceConverter;
import org.saintmartinhospital.fhir.service.patient.converter.PatientConverter;
import org.saintmartinhospital.fhir.service.patient.converter.PatientData;
import org.saintmartinhospital.fhir.service.patient.converter.mappings.PatientDataMapping;
import org.saintmartinhospital.fhir.service.patient.converter.mappings.PatientPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PatientConverterImpl extends AbstractFhirResourceConverter<Person,Patient> implements PatientConverter {
	
	@Autowired
    private ApplicationContext context;
	@Autowired
	private PersonService personService;
	
	private List<PatientDataMapping> mappers;
	private List<PatientPopulator> populators;
	
	@PostConstruct
	private void init() {
		mappers = new ArrayList<>( context.getBeansOfType( PatientDataMapping.class ).values() );
		populators = new ArrayList<>( context.getBeansOfType( PatientPopulator.class ).values() );
	}	

	@Transactional( readOnly = true )
	@Override
	public Patient convertToResource( final Person person ) {
		Patient patient = null;
		if( person != null ) {
			Patient result = new Patient();
			mappers.forEach( mapper -> { mapper.mapTo( result, personService.attach( person ) ); } );
			patient = result;
		}
		return patient;
	}

	@Override
	public Person convertToEntity( final Patient patient ) {
		Person person = null;
		if( patient != null ) {
			Person result = new Person();
			mappers.forEach( mapper -> { mapper.mapFrom( patient, result ); } );
			person = result;
		}
		return person;
	}

	@Override
	public Patient buildPatient( final PatientData patientData ) {
		Patient patient = null;
		if( patientData != null ) {
			final Patient result = new Patient();
			populators.forEach( populator -> { populator.populate( result, patientData ); } );
			patient = result;
		}
		return patient;
	}

}
