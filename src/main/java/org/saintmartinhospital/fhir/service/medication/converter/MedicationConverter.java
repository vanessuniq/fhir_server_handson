package org.saintmartinhospital.fhir.service.medication.converter;

import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Reference;
import org.saintmartinhospital.fhir.common.converter.FhirResourceConverter;
import org.saintmartinhospital.legacy.domain.Medicine;


public interface MedicationConverter extends FhirResourceConverter<Medicine,Medication> {
	
	Reference buildMedicationReference( Integer medicationId );
	Integer getIdFromReference( Reference medicationReference );
	
}
