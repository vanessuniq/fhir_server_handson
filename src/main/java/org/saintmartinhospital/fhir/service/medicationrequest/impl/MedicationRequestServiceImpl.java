package org.saintmartinhospital.fhir.service.medicationrequest.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.HashMap;
import java.util.Map;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.saintmartinhospital.legacy.domain.Prescription;
import org.saintmartinhospital.fhir.common.ListBundleProvider;
import org.saintmartinhospital.fhir.service.medicationrequest.MedicationRequestService;
import org.saintmartinhospital.fhir.service.medicationrequest.converter.MedicationRequestConverter;
import org.saintmartinhospital.legacy.domain.PrescriptionStateEnum;
import org.saintmartinhospital.legacy.service.PrescriptionService;
import org.saintmartinhospital.legacy.service.exceptions.MedicineNotFoundException;
import org.saintmartinhospital.legacy.service.exceptions.PersonNotFoundException;
import org.saintmartinhospital.legacy.service.exceptions.PrescriptionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class MedicationRequestServiceImpl implements MedicationRequestService {

	@Autowired
	private PrescriptionService prescriptionService;
	@Autowired
	private MedicationRequestConverter converter;
	
	private static final Map<Class<? extends Exception>,String> ERROR_MESSAGES = new HashMap<>();
	static { 
		ERROR_MESSAGES.put( PersonNotFoundException.class, "Patient not found" );
		ERROR_MESSAGES.put( MedicineNotFoundException.class, "Medication not found" );
		ERROR_MESSAGES.put( PrescriptionNotFoundException.class, "Medication request not found" );
	}
	
	
	@Override
	public MedicationRequest findById( Integer medicationRequestId ) {
		try {
			MedicationRequest medicationRequest = converter.convertToResource( prescriptionService.findById( medicationRequestId ) );
			if( medicationRequest == null )
				throw new ResourceNotFoundException( "Medication request not found" );
			return medicationRequest;
		} catch( IllegalArgumentException e ) {
			throw new InvalidRequestException( e.getMessage() );
		}
	}

	@Override
	public MedicationRequest create( MedicationRequest medicationRequest ) {
		try {
			medicationRequest = converter.convertToResource( prescriptionService.save( converter.convertToEntity( medicationRequest ) ) );
			if( medicationRequest == null ) 
				throw new IllegalArgumentException( "Invalid medication request" );
			return medicationRequest;
		} catch( PersonNotFoundException | MedicineNotFoundException e ) {
			throw new InvalidRequestException( ERROR_MESSAGES.get( e.getClass() ) );
		}	
	}

	@Override
	public IBundleProvider findByPatientStatus( Integer subjectId, String status ) {
		try {
			PrescriptionStateEnum state = converter.getState( status );
			return new ListBundleProvider( prescriptionService.findByPersonState( subjectId, state ), converter );
		} catch( IllegalArgumentException e ) {
			throw new InvalidRequestException( e.getMessage() );
		}
	}

	@Override
	public MedicationRequest update( MedicationRequest medicationRequest ) {
		try {
			Prescription prescription = converter.convertToEntity( medicationRequest );
			if( prescription == null ) 
				throw new IllegalArgumentException( "Invalid medication request" );
			return converter.convertToResource( prescriptionService.update( prescription ) );
		} catch( PrescriptionNotFoundException e ) {
			throw new ResourceNotFoundException( ERROR_MESSAGES.get( e.getClass() ) );
		} catch( PersonNotFoundException | MedicineNotFoundException e ) {
			throw new InvalidRequestException( ERROR_MESSAGES.get( e.getClass() ) );
		}		
	}
	
}
