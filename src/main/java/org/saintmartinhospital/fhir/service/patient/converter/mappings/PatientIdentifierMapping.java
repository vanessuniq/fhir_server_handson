package org.saintmartinhospital.fhir.service.patient.converter.mappings;

import org.hl7.fhir.r4.model.Reference;


public interface PatientIdentifierMapping extends PatientDataMapping, PatientPopulator {
		
	Reference buildPatientReference( Integer personaId );
	Integer getIdFromReference( Reference patientReference );

}
