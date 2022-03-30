package org.saintmartinhospital.fhir.service.medication;

import org.hl7.fhir.r4.model.Medication;


public interface MedicationService {

	Medication findById( Integer medicationId );
	Medication create( Medication medication );
    // TODO: Micro-Assignment #J-2
			
}
