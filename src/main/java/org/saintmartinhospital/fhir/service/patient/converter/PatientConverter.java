package org.saintmartinhospital.fhir.service.patient.converter;

import org.hl7.fhir.r4.model.Patient;
import org.saintmartinhospital.business.domain.Person;
import org.saintmartinhospital.fhir.common.converter.FhirResourceConverter;


public interface PatientConverter extends FhirResourceConverter<Person,Patient> {
	
	Patient buildPatient( PatientData patientData );
	
}
