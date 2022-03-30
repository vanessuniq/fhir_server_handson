package org.saintmartinhospital.fhir.service.medication.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.Medication;
import org.saintmartinhospital.fhir.service.medication.MedicationService;
import org.saintmartinhospital.fhir.service.medication.converter.MedicationConverter;
import org.saintmartinhospital.legacy.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class MedicationServiceImpl implements MedicationService {

	@Autowired
	private MedicineService medicineService;
	@Autowired
	private MedicationConverter converter;
	
	
	@Transactional
	@Override
	public Medication findById( Integer medicationId ) {
		Medication medication = converter.convertToResource( medicineService.findById( medicationId ) );
		if( medication == null )
			throw new ResourceNotFoundException( String.format( "Medication with identifier %s not found", medicationId ) );
		return medication;
	}

	@Override
	public Medication create( Medication medication ) {
		Validate.notNull( medication, "Unexpected null medication" );
		try {
			return converter.convertToResource( medicineService.save( converter.convertToEntity( medication ) ) );
		} catch( Exception e ) {
            // TODO: Micro-Assignment #J-3
            throw new IllegalStateException();
		}
	}

    // TODO: Micro-Assignment #J-2

}
