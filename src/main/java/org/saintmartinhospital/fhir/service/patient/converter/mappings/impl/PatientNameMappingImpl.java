package org.saintmartinhospital.fhir.service.patient.converter.mappings.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.saintmartinhospital.business.domain.Person;
import org.saintmartinhospital.fhir.service.patient.converter.PatientData;
import org.saintmartinhospital.fhir.service.patient.converter.mappings.PatientNameMapping;
import org.saintmartinhospital.fhir.service.patient.converter.mappings.impl.FamilyExtensionManager.Family;
import org.saintmartinhospital.fhir.service.patient.converter.mappings.impl.FamilyExtensionManager.ParentEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class PatientNameMappingImpl implements PatientNameMapping {
	
	@Autowired
	private FamilyExtensionManager familyExtensionManager;
	

	@Override
	public void mapTo( Patient patient, Person person ) {
		Validate.notNull( patient, "Unexpected null patient" );
		Validate.notNull( person, "Unexpected null person" );
		addOfficialName( patient, person.getFirstName(), person.getSecondName(), person.getFathersLastName(), person.getMothersLastName() );
		addNickName( patient, person.getNickName() );
	}

	@Override
	public void mapFrom( Patient patient, final Person person ) {
		Validate.notNull( patient, "Unexpected null patient" );
		Validate.notNull( person, "Unexpected null person" );
		
		CollectionUtils.emptyIfNull( patient.getName() ).forEach( hname -> {
			if( HumanName.NameUse.OFFICIAL == hname.getUse() ) {
				setNames( person, hname );
				setLastNames( person, hname );
			} else if( HumanName.NameUse.NICKNAME == hname.getUse() ) {
				setNickName( person, hname );
			}
		});
	}
	
	@Override
	public void populate( Patient patient, PatientData patientData ) {
		Validate.notNull( patient, "Unexpected null patient" );
		Validate.notNull( patientData, "Unexpected null patient data" );
		addOfficialName( patient, patientData.getName(), null, patientData.getFathersName(), null );
	}	

/*
 * private methods	
 */	
	
	private void addOfficialName( Patient patient, String firstName, String secondName, String fathersLastName, String mothersLastName ) {
		final HumanName hname = new HumanName();
		hname.setUse( HumanName.NameUse.OFFICIAL );
		
		StringBuilder textBuilder = new StringBuilder();
		StringBuilder familyBuilder = new StringBuilder();
		
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
		final Map<ParentEnum,String> lastNamesMap = new HashMap<>();
		lastNamesMap.put( ParentEnum.FATHER, fathersLastName );
		lastNamesMap.put( ParentEnum.MOTHER, mothersLastName );
		
		Arrays.asList( ParentEnum.FATHER, ParentEnum.MOTHER ).stream().forEach( parentEnum -> {
			StringType family = hname.getFamilyElement();
			String lastName = lastNamesMap.get( parentEnum );
			
			if( StringUtils.isNotBlank( lastName ) ) {
				family.addExtension( familyExtensionManager.getExtension( parentEnum, lastName ) );
				familyBuilder.append( String.format( "%s%s", familyBuilder.length() > 0? StringUtils.SPACE: StringUtils.EMPTY, lastName ) );
			}
		});
		
		textBuilder.append( String.format( " %s", familyBuilder.toString() ) );
		hname.setText( textBuilder.toString() );
		hname.setFamily( familyBuilder.toString() );
		
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
	
	private void setLastNames( Person person, HumanName hname ) {
		Family parents = familyExtensionManager.getFamily( hname );
		if( parents != null ) {
			person.setFathersLastName( parents.getFathersFamily() );
			person.setMothersLastName( parents.getMothersFamily() );
		}
	}
	
	private void setNickName( Person person, HumanName hname ) {
		List<StringType> given = hname.getGiven();
		if( CollectionUtils.isNotEmpty( given ) )
			person.setNickName( given.iterator().next().getValue() );
	}
	
}
