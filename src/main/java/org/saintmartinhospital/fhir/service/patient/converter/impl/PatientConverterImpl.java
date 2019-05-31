package org.saintmartinhospital.fhir.service.patient.converter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.hl7.fhir.r4.model.Patient;
import org.saintmartinhospital.business.domain.Person;
import org.saintmartinhospital.fhir.common.converter.AbstractFhirResourceConverter;
import org.saintmartinhospital.fhir.service.patient.converter.PatientConverter;
import org.saintmartinhospital.fhir.service.patient.converter.mappings.PatientDataMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PatientConverterImpl extends AbstractFhirResourceConverter<Person,Patient> implements PatientConverter {
	
	@Autowired
    private ApplicationContext context;	
	
	private final List<PatientDataMapping> MAPPINGS = new ArrayList<>();
	
	@PostConstruct
	private void init() {
		Map<String,PatientDataMapping> map = context.getBeansOfType( PatientDataMapping.class );
		for( PatientDataMapping curMapping: map.values() )
			MAPPINGS.add( curMapping );
	}	

	@Transactional
	@Override
	public Patient convertToResource( Person person ) {
		Patient patient = null;
		if( person != null ) {
			patient = new Patient();
			for( PatientDataMapping curMapping: MAPPINGS )
				curMapping.mapTo( patient, person );
		}
		return patient;
	}

	@Override
	public Person convertToEntity(Patient fhirResource) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
