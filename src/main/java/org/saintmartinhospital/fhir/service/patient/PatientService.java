package org.saintmartinhospital.fhir.service.patient;

import org.hl7.fhir.r4.model.Patient;

public interface PatientService {

	Patient findById( Long patientId );	
	
}
