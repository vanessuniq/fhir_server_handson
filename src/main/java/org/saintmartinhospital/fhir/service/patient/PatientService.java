package org.saintmartinhospital.fhir.service.patient;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import java.util.Calendar;
import org.hl7.fhir.r4.model.Patient;

public interface PatientService {

	Patient findById( Integer patientId );
	IBundleProvider findByCriteria( String name, String fathersFamily, String docSystem, String docValue, Calendar birthdate, String gender ) throws IllegalArgumentException;
	Integer create( Patient patient );
    // TODO: Micro-Assignment #J-1
    
}
