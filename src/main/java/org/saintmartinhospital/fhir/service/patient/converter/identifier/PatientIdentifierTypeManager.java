package org.saintmartinhospital.fhir.service.patient.converter.identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.UriType;
import static org.saintmartinhospital.fhir.service.patient.converter.identifier.PatientIdentifierTypeEnum.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
public class PatientIdentifierTypeManager {
	
	private final List<PatientIdentifierInfo> identifierInfo = new ArrayList<>();
			
	@Value("${fhir.identifier.patient.id.baseurl}")
	private String patient_id_baseurl;
	@Value("${fhir.identifier.patient.ni.baseurl}")
	private String ni_id_baseurl;
	@Value("${fhir.identifier.patient.pp.baseurl}")
	private String pp_id_baseurl;

	
	@EventListener
	private void init( ApplicationReadyEvent event ) {
		identifierInfo.add( new PatientIdentifierInfo( PATIENT_ID, patient_id_baseurl, PATIENT_ID.getName(), Identifier.IdentifierUse.OFFICIAL ) );
		identifierInfo.add( new PatientIdentifierInfo( NI, ni_id_baseurl, NI.getName(), Identifier.IdentifierUse.USUAL ) );
		identifierInfo.add( new PatientIdentifierInfo( PP, pp_id_baseurl, PP.getName(), Identifier.IdentifierUse.USUAL ) );
	}
	
	public String formatIdValue( PatientIdentifierTypeEnum identifierType, Object value ) {
		PatientIdentifierInfo info = findByIdentifierType( identifierType );
		Validate.notNull( info, "Unkown identifier %s for value %s", identifierType, value );
		return String.format( "%s|%s", info.getUriTypeAsString(), value.toString() );
	}
	
	public PatientIdentifierInfo findByUrl( final String url ) {
		PatientIdentifierInfo idInfo = null;

		if( StringUtils.isNotEmpty( url ) ) {
			Optional<PatientIdentifierInfo> optionalInfo = identifierInfo.stream().filter( info -> info.getUriTypeAsString().equals( url ) ).findFirst();
			if( !Optional.empty().equals( optionalInfo ) )
				idInfo = optionalInfo.get();
		}
		
		return idInfo;
	}
	
	public PatientIdentifierInfo findByIdentifierType( final PatientIdentifierTypeEnum identifierType ) {
		PatientIdentifierInfo idInfo = null;
		
		if( identifierType != null ) {
			Optional<PatientIdentifierInfo> optionalInfo = identifierInfo.stream().filter( info -> info.getIdentifierType().equals( identifierType ) ).findFirst();
			if( !Optional.empty().equals( optionalInfo ) )
				idInfo = optionalInfo.get();
		}
		
		return idInfo;
	}

}
