package org.saintmartinhospital.fhir.service.patient.impl;

import org.hl7.fhir.r4.model.Patient;
import org.saintmartinhospital.business.service.PersonService;
import org.saintmartinhospital.fhir.service.patient.PatientService;
import org.saintmartinhospital.fhir.service.patient.converter.PatientConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PatientServiceImpl implements PatientService {
	
	@Autowired
	private PersonService personService;
	@Autowired
	private PatientConverter converter;

	@Transactional
	@Override
	public Patient findById( Integer patientId ) {
		return converter.convertToResource( personService.findById( patientId ) );
	}

}
