package org.saintmartinhospital.fhir.resource;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.saintmartinhospital.fhir.common.FhirResourceUtils;
import org.saintmartinhospital.fhir.service.patient.PatientService;
import org.saintmartinhospital.fhir.service.patient.converter.PatientIdentifierTypeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PatientResource implements IResourceProvider {
	
	@Autowired
	private PatientService patientService;
	@Autowired
	private PatientIdentifierTypeManager typeManager;
	

	@Override
	public Class<Patient> getResourceType() {
		return Patient.class;
	}
	
	@Read()
	public Patient findById( @IdParam IdType id ) {
		Long patientId = FhirResourceUtils.getLong( id );
		Patient patient = patientService.findById( patientId );
		if( patient == null )
			throw new ResourceNotFoundException( String.format( "Patient with identifier %s not found", typeManager.formatIdValue( patientId ) ) );
		return patient;
	}	
	
}
