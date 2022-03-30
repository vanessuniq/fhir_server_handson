package org.saintmartinhospital.fhir.service.patient.converter.mappings;

import org.hl7.fhir.r4.model.Patient;
import org.saintmartinhospital.legacy.domain.Person;


public interface PatientDataMapping {
	
	// Map from person to patient
	void mapTo( Patient patient, Person person );
	// Map from patient to person
	void mapFrom( Patient patient, Person person );
	
}
