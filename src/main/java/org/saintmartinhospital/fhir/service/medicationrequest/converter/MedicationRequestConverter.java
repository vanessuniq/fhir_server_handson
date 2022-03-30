package org.saintmartinhospital.fhir.service.medicationrequest.converter;

import org.hl7.fhir.r4.model.MedicationRequest;
import org.saintmartinhospital.fhir.common.converter.FhirResourceConverter;
import org.saintmartinhospital.legacy.domain.PrescriptionStateEnum;
import org.saintmartinhospital.legacy.domain.Prescription;


public interface MedicationRequestConverter extends FhirResourceConverter<Prescription,MedicationRequest> {
	
	PrescriptionStateEnum getState( String status );
	
}
