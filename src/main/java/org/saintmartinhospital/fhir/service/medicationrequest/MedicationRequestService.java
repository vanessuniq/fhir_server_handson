package org.saintmartinhospital.fhir.service.medicationrequest;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.r4.model.MedicationRequest;


public interface MedicationRequestService {

	MedicationRequest findById( Integer medicationRequestId );
	MedicationRequest create( MedicationRequest medicationRequest );
	IBundleProvider findByPatientStatus( Integer patientId, String status );
	MedicationRequest update( MedicationRequest medicateionRequest );
	
}
