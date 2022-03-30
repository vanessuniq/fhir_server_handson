package org.saintmartinhospital.fhir.service.patient.converter.mappings.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.fhir.service.patient.converter.PatientData;
import org.saintmartinhospital.fhir.service.patient.converter.mappings.PatientNameMapping;
import org.springframework.stereotype.Component;


@Component
public class PatientNameMappingImpl implements PatientNameMapping {
	
	@Override
	public void mapTo( Patient patient, Person person ) {
		Validate.notNull( patient, "Unexpected null patient" );
		Validate.notNull( person, "Unexpected null person" );
		addOfficialName( patient, person.getFirstName(), person.getSecondName(), person.getLastName() );
		addNickName( patient, person.getNickName() );
	}

	@Override
	public void mapFrom( Patient patient, final Person person ) {
		Validate.notNull( patient, "Unexpected null patient" );
		Validate.notNull( person, "Unexpected null person" );
		
		CollectionUtils.emptyIfNull( patient.getName() ).forEach( hname -> {
			if( HumanName.NameUse.OFFICIAL == hname.getUse() ) {
				setNames( person, hname );
				setLastName( person, hname );
			} else if( HumanName.NameUse.NICKNAME == hname.getUse() ) {
				setNickName( person, hname );
			}
		});
	}
	
	@Override
	public void populate( Patient patient, PatientData patientData ) {
		Validate.notNull( patient, "Unexpected null patient" );
		Validate.notNull( patientData, "Unexpected null patient data" );
		addOfficialName( patient, patientData.getName(), null, patientData.getLastName() );
	}	

/*
 * private methods	
 */	
	
	private void addOfficialName( Patient patient, String firstName, String secondName, String lastName ) {
		final HumanName hname = new HumanName();
		hname.setUse( HumanName.NameUse.OFFICIAL );
		
		StringBuilder textBuilder = new StringBuilder();
		
		// Set names
		final List<StringType> names = new ArrayList<>();
		
		Arrays.asList( firstName, secondName ).stream().forEach( name -> {
			if( StringUtils.isNotBlank( name ) ) {
				textBuilder.append( String.format( "%s%s", textBuilder.length() > 0? StringUtils.SPACE: StringUtils.EMPTY, name ) );
				names.add( new StringType( name ) );
			}
		});
		hname.setGiven( names );
		
		// Set last names
		hname.setFamily( lastName );
        
		textBuilder.append( String.format( " %s", lastName ) );
		hname.setText( textBuilder.toString() );
		
		patient.addName( hname );
	}
	
	private void addNickName( Patient patient, String nickName ) {
		if( StringUtils.isNotBlank( nickName ) ) {
			HumanName hname = new HumanName();
			hname.setUse( HumanName.NameUse.NICKNAME );
			hname.setGiven( Collections.singletonList( new StringType( nickName ) ) );
			patient.addName( hname );
		}
	}

	private void setNames( Person person, HumanName hname ) {
		List<StringType> given = hname.getGiven();
		if( CollectionUtils.isNotEmpty( given ) ) {
			Iterator<StringType> iterator = given.iterator();
			person.setFirstName( iterator.next().getValue() );
			if( iterator.hasNext() )
				person.setSecondName( iterator.next().getValue() );
		}
	}
	
	private void setLastName( Person person, HumanName hname ) {
		person.setLastName( hname.getFamily() );
	}
	
	private void setNickName( Person person, HumanName hname ) {
		List<StringType> given = hname.getGiven();
		if( CollectionUtils.isNotEmpty( given ) )
			person.setNickName( given.iterator().next().getValue() );
	}
	
}
