package org.saintmartinhospital.fhir.service.patient.converter.mappings.impl;

import java.util.Calendar;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.saintmartinhospital.business.domain.Person;
import org.saintmartinhospital.business.domain.PersonDoc;
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
				String docAbrev = personDoc.getDocType().getAbrev();
				PatientIdentifierTypeEnum idType = PatientIdentifierTypeEnum.valueOf( docAbrev );
				if( idType == null )
					throw new IllegalStateException( String.format( "Unexpected patient identifier type %s", docAbrev ) );
				
				addIdentifier( patient, idType, personDoc.getDocValue(), personDoc.getCreateDate(), personDoc.getDeleteDate() );
			}
	}

	@Override
	public void mapFrom( Patient patient, Person person ) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
/*
 * private methods
 */	
	
	private void addIdentifier( Patient patient, PatientIdentifierTypeEnum idType, Object value, Calendar create, Calendar delete ) {
		Validate.notNull( patient, "Unexpected null patient" );
		Validate.notNull( idType, "Unexpected null type identifier" );
		Validate.notNull( value, "Unexpected null value identifier" );
		
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
