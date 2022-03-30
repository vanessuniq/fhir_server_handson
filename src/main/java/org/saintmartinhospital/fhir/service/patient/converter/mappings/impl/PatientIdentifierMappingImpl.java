package org.saintmartinhospital.fhir.service.patient.converter.mappings.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.saintmartinhospital.legacy.domain.DocType;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.legacy.domain.PersonDoc;
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

	private static final String REFERENCE_PREFIX = "Patient/";	
	
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
					throw new IllegalArgumentException( String.format( "Unexpected %s system identifier", personDoc.getDocType().getAbrev() ) );
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
						Validate.isTrue( ( StringUtils.isEmpty( patient.getId() ) && StringUtils.isEmpty( identifier.getValue() ) ) || patient.getIdElement().getIdPart().equals( identifier.getValue() ),
							"%s does not correspond to \"%s\"", patient.getId(), typeManager.formatIdValue( PatientIdentifierTypeEnum.PATIENT_ID, identifier.getValue() ) );
						person.setId( Integer.parseInt( identifier.getValue() ) );
					} catch( NumberFormatException e ) {
						throw new IllegalArgumentException( String.format( "The value of the system identifier \"%s\" can only be a number", idInfo.getUriTypeAsString() ), e );
					}
				else {
					PersonDoc personDoc = new PersonDoc();
					personDoc.setDocType( new DocType( idInfo.getIdentifierType().toString() ) );
					personDoc.setDocValue( identifier.getValue() );
					
					Period period = identifier.getPeriod();
					if( period != null ) {
						if( period.getStart() != null && period.getEnd() != null )
							Validate.isTrue( period.getStart().before( period.getEnd() ), "The start date must occur before than the end date inside \"%s\" system identifier", identifier.getSystem() );
						personDoc.setCreateDate( toCalendar( period.getStart() ) );
						personDoc.setDeleteDate( toCalendar( period.getEnd() ) );
					}
										
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
	
	@Override
	public Reference buildPatientReference( Integer personaId ) {
		Reference reference = null;
		if( personaId != null ) {
			reference = new Reference();
			Patient patient = new Patient();
			addIdentifier( patient, PATIENT_ID, personaId, null, null );
			reference.setIdentifier( patient.getIdentifier().iterator().next() );
			reference.setResource( patient );
		}
		return reference;
	}
	
	@Override
	public Integer getIdFromReference( Reference patientReference ) {
		Integer id = null;
		if( patientReference != null ) {
			try {
				String relativePath = patientReference.getReference();
				Validate.isTrue( StringUtils.isNotEmpty( relativePath ), "Empty patient reference" );
				Validate.isTrue( relativePath.indexOf( REFERENCE_PREFIX ) == 0, "Wrong patient reference \"%s\"", relativePath );
				String idAsString = relativePath.substring( REFERENCE_PREFIX.length() );
				id = Integer.parseInt( idAsString );
			} catch( NumberFormatException e ) {
				throw new IllegalArgumentException( String.format( "The id \"%s\" is not valid patient identifier, number expected instead", patientReference.getId() ) );
			}
		}
		return id;
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
	
	private Calendar toCalendar( Date date ) {
		Calendar cal = null;
		if( date != null ) {
			cal = Calendar.getInstance();
			cal.setTime( date );
		}
		return cal;
	}
			

}
