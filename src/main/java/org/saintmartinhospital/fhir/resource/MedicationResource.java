package org.saintmartinhospital.fhir.resource;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Medication;
import org.saintmartinhospital.fhir.common.FhirResourceUtils;
import org.saintmartinhospital.fhir.service.medication.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class MedicationResource implements IResourceProvider {

	private static final String RESOURCE_TYPE = "Medication";
	
	@Autowired
	private MedicationService medicationService;
	
	
	@Override
	public Class<Medication> getResourceType() {
		return Medication.class;
	}
	
	@Read()
	public Medication findById( @IdParam IdType id ) {
		Integer medicationId = FhirResourceUtils.getInteger( id );
		return medicationService.findById( medicationId );
	}
	
	@Create
	public MethodOutcome create( @ResourceParam Medication medication ) {
		MethodOutcome outcome = new MethodOutcome();
		medication = medicationService.create( medication );
		outcome.setId( new IdType( RESOURCE_TYPE, medication.getId() ) );
		outcome.setResource( medication );
		return outcome;		
	}
	
    // TODO: Micro-Assignment #J-2

}
