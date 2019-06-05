package org.saintmartinhospital.fhir.service.patient.converter.mappings.impl;

import java.util.Calendar;
import java.util.HashSet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.saintmartinhospital.business.domain.DocType;
import org.saintmartinhospital.business.domain.Person;
import org.saintmartinhospital.business.domain.PersonDoc;
import static org.saintmartinhospital.business.domain.PersonDoc_.docType;
import org.saintmartinhospital.fhir.service.patient.converter.PatientData;
import org.saintmartinhospital.fhir.service.patient.converter.mappings.PatientIdentifierMapping;
import org.saintmartinhospital.fhir.service.patient.converter.identifier.PatientIdentifierInfo;
import org.saintmartinhospital.fhir.service.patient.converter.identifier.PatientIdentifierTypeEnum;
import static org.saintmartinhospital.fhir.service.patient.converter.identifier.PatientIdentifierTypeEnum.*;
import org.saintmartinhospital.fhir.service.patient.converter.identifier.PatientIdentifierTypeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class PatientIdentifierMappingImpl implements PatientIdentifierMapping {

	@Autowired
	private PatientIdentifierTypeManager typeManager;
	
	@Transactional
	@Override
	public void mapTo( Patient patient, Person person ) {
		Validate.notNull( patient, "Null patient" );
		Validate.notNull( person, "Null person" );
		
		// Add the patient-id identifier
		addIdentifier( patient, PATIENT_ID, person.getId(), person.getCreateDate(), null );
		
		if( CollectionUtils.isNotEmpty( person.getDocs() ) )
			for( PersonDoc personDoc: person.getDocs() ) {
				PatientIdentifierTypeEnum idType = PatientIdentifierTypeEnum.valueOf( personDoc.getDocType().getAbrev() );
				if( idType == null )
					throw new IllegalArgumentException( String.format( "Unexpected %s system identifier" ) );
				addIdentifier( patient, idType, personDoc.getDocValue(), personDoc.getCreateDate(), personDoc.getDeleteDate() );
			}
	}

	@Override
	public void mapFrom( Patient patient, Person person ) {
		if( CollectionUtils.isNotEmpty( patient.getIdentifier() ) ) {
			person.setDocs( new HashSet<>() );
			
			for( Identifier identifier: patient.getIdentifier() ) {
				PatientIdentifierInfo idInfo = typeManager.findByUrl( identifier.getSystem() );
				if( idInfo == null )
					throw new IllegalArgumentException( String.format( "Unexpected system identifier %s", identifier.getSystem() ) );
				
				if( idInfo.getIdentifierType() == PatientIdentifierTypeEnum.PATIENT_ID )
					try {
						person.setId( Integer.parseInt( identifier.getValue() ) );
					} catch( NumberFormatException e ) {
						throw new IllegalArgumentException( String.format( "The value of the system identifier \"%s\" can only be a number", idInfo.getUriTypeAsString() ), e );
					}
				else {
					PersonDoc personDoc = new PersonDoc();
					personDoc.setDocType( new DocType( idInfo.getIdentifierType().toString() ) );
					personDoc.setDocValue( identifier.getValue() );
					person.getDocs().add( personDoc );
				}
			}
		}
	}
	
	@Override
	public void populate( Patient patient, PatientData patientData ) {
		Validate.notNull( patient, "Unexpected null patient" );
		Validate.notNull( patientData, "Unexpected null patient data" );
		
		if( StringUtils.isNotBlank( patientData.getDocSystem() ) && StringUtils.isNotBlank( patientData.getDocValue() ) ) {
			PatientIdentifierInfo idInfo = typeManager.findByUrl( patientData.getDocSystem() );
			if( idInfo == null )
				throw new IllegalArgumentException( String.format( "The URI \"%s\" is not a valid system identifier", patientData.getDocSystem() ) );
			
			addIdentifier( patient, idInfo.getIdentifierType(), patientData.getDocValue(), null, null );
		}
	}	
	
/*
 * private methods
 */	
	
	private void addIdentifier( Patient patient, PatientIdentifierTypeEnum idType, Object value, Calendar create, Calendar delete ) {
		if( patient != null && idType != null && value != null ) {
			PatientIdentifierInfo idInfo = typeManager.findByIdentifierType( idType );
			if( idInfo == null )
				throw new IllegalStateException( String.format( "Can't find information about %s identifier", idType ) );

			if( idInfo.getIdentifierType().equals( PATIENT_ID ) )
				patient.setId( new IdType( value.toString() ) );

			Identifier identifier = patient.addIdentifier();
			identifier.setUse( idInfo.getUse() );
			identifier.setSystemElement( idInfo.getUriType() );
			identifier.setValue( value.toString() );

			if( create != null ) {
				Period period = new Period();
				period.setStart( create.getTime() );
				if( delete != null )
					period.setEnd( delete.getTime() );
				identifier.setPeriod( period );
			}
		}
	}

}
