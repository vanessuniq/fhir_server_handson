package org.saintmartinhospital.fhir.resource;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.saintmartinhospital.fhir.common.FhirResourceUtils;
import org.saintmartinhospital.fhir.service.medicationrequest.MedicationRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class MedicationRequestResource implements IResourceProvider {

	private static final String RESOURCE_TYPE = "MedicationRequest";
	
	@Autowired
	private MedicationRequestService medicationRequestService;

	
	@Override
	public Class<MedicationRequest> getResourceType() {
		return MedicationRequest.class;
	}
	
	@Read()
	public MedicationRequest findById( @IdParam IdType id ) {
		Integer medicationRequestId = FhirResourceUtils.getInteger( id );
		return medicationRequestService.findById( medicationRequestId );
	}
	
	@Create
	public MethodOutcome create( @ResourceParam MedicationRequest medicationRequest ) {
		MethodOutcome outcome = new MethodOutcome();
		medicationRequest = medicationRequestService.create( medicationRequest );
		outcome.setId( new IdType( RESOURCE_TYPE, medicationRequest.getId() ) );
		outcome.setResource( medicationRequest );
		return outcome;
	}
	
	@Search
	public IBundleProvider findByPatientStatus( @RequiredParam( name = MedicationRequest.SP_SUBJECT ) IdType patientIdParam, @OptionalParam( name = MedicationRequest.SP_STATUS ) StringParam statusParam ) {
		Integer patientId = FhirResourceUtils.getInteger( patientIdParam );
		String status = FhirResourceUtils.getString( statusParam );
		return medicationRequestService.findByPatientStatus( patientId, status );
	}
	
	@Update
	public MethodOutcome udpate( @IdParam IdType id, @ResourceParam MedicationRequest medicationRequest ) {
		try {
			Validate.isTrue( id.getIdPartAsLong() > 0, "The id parameter must be an integer greater than 0" );
			Validate.isTrue( StringUtils.isNotEmpty( medicationRequest.getId() ) && medicationRequest.getIdElement().getIdPartAsLong() > 0, "The medication request id must be an integer greater than 0" );
			Validate.isTrue( id.getIdPartAsLong().equals( medicationRequest.getIdElement().getIdPartAsLong() ), "The id paramter must be equal to the medication request id" );
			medicationRequest = medicationRequestService.update( medicationRequest );
			MethodOutcome outcome = new MethodOutcome();
			outcome.setResource( medicationRequest );
			return outcome;
		} catch( IllegalArgumentException e ) {
			throw new InvalidRequestException( e.getMessage() );
		}
	}
	
}
