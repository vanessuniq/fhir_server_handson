package org.saintmartinhospital.fhir.service.patient.converter.identifier;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.UriType;

/**
 * This class it's a mapping between the person document and the patient identifier's URI
 */
@Getter
public class PatientIdentifierInfo {
	
	private static final String SEP = "/";

	private final PatientIdentifierTypeEnum identifierType;
	private final UriType uriType;
	private final String uriTypeAsString;
	private final Identifier.IdentifierUse use;
	
	protected PatientIdentifierInfo( PatientIdentifierTypeEnum identifierType, String uri, Identifier.IdentifierUse use ) {
		Validate.notNull( identifierType );
		Validate.notEmpty( uri );
		
		this.identifierType = identifierType;
		this.uriType = new UriType( uri );
		this.uriTypeAsString = uri;
		this.use = use;
	}
	
	protected PatientIdentifierInfo( PatientIdentifierTypeEnum identifierType, String baseUri, String path, Identifier.IdentifierUse use ) {
		Validate.notEmpty( baseUri );
		Validate.notEmpty( path );
		Validate.notNull( use );
		
		String uri = String.format( "%s%s%s", baseUri, baseUri.endsWith( SEP )? StringUtils.EMPTY: SEP, path );
		this.identifierType = identifierType;
		this.uriType = new UriType( uri );
		this.uriTypeAsString = uri;
		this.use = use;
	}
	
}
