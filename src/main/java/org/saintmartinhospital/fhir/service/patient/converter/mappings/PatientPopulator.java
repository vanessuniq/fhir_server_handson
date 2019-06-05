package org.saintmartinhospital.fhir.service.patient.converter.mappings;

import org.hl7.fhir.r4.model.Patient;
import org.saintmartinhospital.fhir.service.patient.converter.PatientData;


public interface PatientPopulator {

	void populate( Patient patient, PatientData patientData );	
	
}
