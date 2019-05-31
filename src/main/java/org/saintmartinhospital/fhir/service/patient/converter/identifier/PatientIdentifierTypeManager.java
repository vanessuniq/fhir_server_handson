package org.saintmartinhospital.fhir.service.patient.converter.identifier;

import java.util.ArrayList;
import java.util.List;
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
		identifierInfo.add( new PatientIdentifierInfo( PATIENT_ID, patient_id_baseurl, PATIENT_ID.getName().toLowerCase(), Identifier.IdentifierUse.OFFICIAL ) );
		identifierInfo.add( new PatientIdentifierInfo( NI, ni_id_baseurl, NI.getName().toLowerCase(), Identifier.IdentifierUse.USUAL ) );
		identifierInfo.add( new PatientIdentifierInfo( PP, pp_id_baseurl, PP.getName().toLowerCase(), Identifier.IdentifierUse.USUAL ) );
	}
	
	public String formatIdValue( PatientIdentifierTypeEnum identifierType, Object value ) {
		PatientIdentifierInfo info = findByIdentifierType( identifierType );
		Validate.notNull( info, "Unkown identifier %s for value %s", identifierType, value );
		return String.format( "%s|%s", info.getUriTypeAsString(), value.toString() );
	}
	
	public PatientIdentifierInfo findByUrl( String url ) {
		PatientIdentifierInfo found = null;
		if( StringUtils.isNotEmpty( url ) )
			for( PatientIdentifierInfo curInfo: identifierInfo )
				if( curInfo.getUriTypeAsString().equals( url ) ) {
					found = curInfo;
					break;
				}
		return found;
	}
	
	public PatientIdentifierInfo findByIdentifierType( PatientIdentifierTypeEnum identifierType ) {
		PatientIdentifierInfo found = null;
		if( identifierType != null )
			for( PatientIdentifierInfo curInfo: identifierInfo )
				if( curInfo.getIdentifierType().equals( identifierType ) ) {
					found = curInfo;
					break;
				}
		return found;
	}

}
