package org.saintmartinhospital.fhir.service.patient.converter;

import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.fhir.common.converter.FhirResourceConverter;


public interface PatientConverter extends FhirResourceConverter<Person,Patient> {
	
	Patient buildPatient( PatientData patientData );
	Reference buildPatientReference( Integer personaId );
	Integer getIdFromReference( Reference patientReference );		
	
}
