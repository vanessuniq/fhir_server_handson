package org.saintmartinhospital.fhir.service.patient.impl;

import org.hl7.fhir.r4.model.Patient;
import org.saintmartinhospital.fhir.service.patient.PatientService;
import org.springframework.stereotype.Component;

@Component
public class PatientServiceImpl implements PatientService {

	@Override
	public Patient findById( Long patientId ) {
		return null;
	}

}
