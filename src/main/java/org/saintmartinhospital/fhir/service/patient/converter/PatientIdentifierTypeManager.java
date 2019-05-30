package org.saintmartinhospital.fhir.service.patient.converter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 *
 * @author julian.martinez
 */
@Component
public class PatientIdentifierTypeManager {
	
	private static final String PATIENT_ID_NAME = "patient-id";
			
	@Value("${hospital.url}")
	private String HOSPITAL_URL;
	private String PATIENT_ID;
	
	@EventListener
	private void init( ApplicationReadyEvent event ) {
		PATIENT_ID = HOSPITAL_URL + PATIENT_ID_NAME;
	}
	
	public String formatIdValue( Long value ) {
		return String.format( "%s|%d", PATIENT_ID, value );
	}
	

}
