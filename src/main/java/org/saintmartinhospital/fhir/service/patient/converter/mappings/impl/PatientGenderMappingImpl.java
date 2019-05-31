package org.saintmartinhospital.fhir.service.patient.converter.mappings.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Patient;
import org.saintmartinhospital.business.domain.GenderEnum;
import org.saintmartinhospital.business.domain.Person;
import org.saintmartinhospital.fhir.service.patient.converter.mappings.PatientGenderMapping;
import org.springframework.stereotype.Component;



@Component
public class PatientGenderMappingImpl implements PatientGenderMapping {
	
	@Getter
	private static class GenderMap {
		private final GenderEnum personGender;
		private final AdministrativeGender patientGender;
		
		public GenderMap( GenderEnum personGender, AdministrativeGender patientGender ) {
			this.personGender = personGender;
			this.patientGender = patientGender;
		}
	}
	
	private static final List<GenderMap> GENDER_LIST = new ArrayList<>();
	static {
		GENDER_LIST.add( new GenderMap( GenderEnum.MALE, AdministrativeGender.MALE ) );
		GENDER_LIST.add( new GenderMap( GenderEnum.FEMALE, AdministrativeGender.FEMALE ) );
		GENDER_LIST.add( new GenderMap( GenderEnum.OTHER, AdministrativeGender.OTHER ) );
	}

	@Override
	public void mapTo( Patient patient, Person person ) {
		Validate.notNull( patient, "Unexpected null patient" );
		Validate.notNull( person, "Unexpected null person" );
		
		Optional<GenderMap> optional = GENDER_LIST.stream().filter( genderMap -> genderMap.getPersonGender().equals( person.getGender() ) ).findFirst();
		Validate.validState( !Optional.empty().equals( optional ), "Unexpected %s person gender", person.getGender() );
		patient.setGender( optional.get().getPatientGender() );
	}

	@Override
	public void mapFrom( Patient patient, Person person ) {
		Validate.notNull( patient, "Unexpected null patient" );
		Validate.notNull( person, "Unexpected null person" );
		
		Optional<GenderMap> optional = GENDER_LIST.stream().filter( genderMap -> genderMap.getPatientGender().equals( patient.getGender() ) ).findFirst();
		Validate.validState( !Optional.empty().equals( optional ), "Unexpected %s patient gender", patient.getGender() );
		person.setGender( optional.get().getPersonGender() );
	}

}
