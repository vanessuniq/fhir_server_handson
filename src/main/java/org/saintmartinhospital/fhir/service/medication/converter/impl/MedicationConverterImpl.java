package org.saintmartinhospital.fhir.service.medication.converter.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.UriType;
import org.saintmartinhospital.fhir.common.converter.AbstractFhirResourceConverter;
import org.saintmartinhospital.fhir.service.medication.converter.MedicationConverter;
import org.saintmartinhospital.legacy.domain.Medicine;
import org.saintmartinhospital.legacy.domain.MedicineSystemEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
public class MedicationConverterImpl extends AbstractFhirResourceConverter<Medicine,Medication> implements MedicationConverter {

	private static final String REFERENCE_PREFIX = "Medication/";	
	private static final String MEDICATION_ID_NAME = "medication-id";
	
	@Value("${fhir.hospital.url}")
	private String medicationIdBaseUrl;
	private UriType medicationUriType;
	
	@Getter
	private static class SystemMap {
		private final MedicineSystemEnum system;
		private final String url;
		
		public SystemMap( MedicineSystemEnum system, String url ) {
			this.system = system;
			this.url = url;
		}
	}
	
	private static final List<SystemMap> SYSTEM_LIST = new ArrayList<>();
	static {
		SYSTEM_LIST.add( new SystemMap( MedicineSystemEnum.RXNORM, "http://www.nlm.nih.gov/research/umls/rxnorm" ) );
		SYSTEM_LIST.add( new SystemMap( MedicineSystemEnum.SNOMED, "http://snomed.info/sct" ) );
	}
	
	
	@EventListener
	protected void init( ApplicationReadyEvent event ) {
		medicationUriType = new UriType( String.format( "%s%s", medicationIdBaseUrl, MEDICATION_ID_NAME ) );
	}
	
	@Override
	public Medication convertToResource( Medicine medicine ) {
		Medication medication = null;
		if( medicine != null ) {
			medication = new Medication();
			addIdentifiers( medication, medicine );
			addCodeableConcept( medication, medicine );
			addNarrative( medication );
		}
		return medication;
	}

	@Override
	public Medicine convertToEntity( Medication medication ) {
		Medicine medicine = null;
		if( medication != null ) {
			medicine = new Medicine();
			addId( medicine, medication );
			addOtherProperties( medicine, medication );
		}
		return medicine;
	}
	
	@Override
	public Reference buildMedicationReference( Integer medicineId ) {
		Reference reference = null;
		if( medicineId != null ) {
			reference = new Reference();
			Medication medication = new Medication();
			Medicine medicine = new Medicine();
			medicine.setId( medicineId );
			addIdentifiers( medication, medicine );
			reference.setIdentifier( medication.getIdentifier().iterator().next() );
			reference.setResource( medication );
		}
		return reference;
	}

	@Override
	public Integer getIdFromReference( Reference medicationReference ) {
		try {
			Validate.notNull( medicationReference, "Unexpected null medication reference" );
			String relativePath = medicationReference.getReference();
			Validate.isTrue( StringUtils.isNotEmpty( relativePath ), "Empty medication reference" );
			Validate.isTrue( relativePath.indexOf( REFERENCE_PREFIX ) == 0, "Wrong medication reference \"%s\"", relativePath );
			String idAsString = relativePath.substring( REFERENCE_PREFIX.length() );			
			return Integer.parseInt( idAsString );
		} catch( NumberFormatException e ) {
			throw new IllegalArgumentException( String.format( "The id \"%s\" is not valid medication identifier, number expected instead", medicationReference.getId() ) );
		}
	}
	
/*
 * private methods	
 */	
	
	private void addIdentifiers( Medication medication, Medicine medicine ) {
		List<Identifier> identifiers = null;
		
		if( medicine.getId() != null ) {
			medication.setId( new IdType( medicine.getId().toString() ) );			
			
			Identifier identifier = new Identifier();
			identifier.setValue( medication.getId() );
			identifier.setUse( Identifier.IdentifierUse.OFFICIAL );
			identifier.setSystemElement( medicationUriType );

			if( medicine.getCreateDate() != null ) {
				Period period = new Period();
				period.setStart( medicine.getCreateDate().getTime() );
				identifier.setPeriod( period );
			}

			identifiers = Collections.singletonList( identifier );
		}
		
		medication.setIdentifier( identifiers );
	}
	
	private void addCodeableConcept( Medication medication, Medicine medicine ) {
		CodeableConcept codeable = new CodeableConcept();
		
		// Find the url that corresponds to the system
		Optional<SystemMap> optional = SYSTEM_LIST.stream().filter( systemMap -> systemMap.getSystem().equals( medicine.getSystem() ) ).findFirst();
		Validate.isTrue( !Optional.empty().equals( optional ), "Unexpected %s medicine system", medicine.getSystem() );
		SystemMap systemMap = optional.get();
		
		Coding coding = new Coding();
		coding.setSystem( systemMap.getUrl() );
		coding.setCode( medicine.getCode() );
		coding.setDisplay( medicine.getDescription() );

		codeable.setCoding( Collections.singletonList( coding ) );
		codeable.setText( medicine.getDescription() );
		
		medication.setCode( codeable );
	}

	private void addId( Medicine medicine, Medication medication ) {
		if( CollectionUtils.isNotEmpty( medication.getIdentifier() ) ) {
			String medicationUri = medicationUriType.getValue();			
			try {
				Identifier identifier = medication.getIdentifier().iterator().next();
				Validate.isTrue( medicationUri.equals( identifier.getSystem() ), "Unexpected medication system identifier %s", medicationUri );
				medicine.setId( Integer.parseInt( identifier.getId() ) );
				
				if( identifier.getPeriod() != null ) {
					Calendar createDate = Calendar.getInstance();
					createDate.setTime( identifier.getPeriod().getStart() );
					medicine.setCreateDate( createDate );
				}				
			} catch( NumberFormatException e ) {
				throw new IllegalArgumentException( String.format( "The value of the system identifier \"%s\" can only be a number", medicationUri ), e );
			}
		}
	}
	
	private void addOtherProperties( Medicine medicine, Medication medication ) {
		if( medication.getCode() != null && CollectionUtils.isNotEmpty( medication.getCode().getCoding() ) ) {
			Coding coding = medication.getCode().getCoding().iterator().next();
			
			// Find the system that corresponds to the url
			Optional<SystemMap> optional = SYSTEM_LIST.stream().filter( systemMap -> systemMap.getUrl().equals( coding.getSystem() ) ).findFirst();
			Validate.isTrue( !Optional.empty().equals( optional ), "Unexpected medication codeable system %s", coding.getSystem() );
			SystemMap systemMap = optional.get();
			
			Validate.isTrue( StringUtils.isNotEmpty( coding.getCode() ), "Unexpected empty code" );
			medicine.setCode( coding.getCode() );
			medicine.setSystem( systemMap.getSystem() );
			medicine.setDescription( coding.getDisplay() );
		}
	}
	
	private void addNarrative( Medication medication ) {
		if( CollectionUtils.isNotEmpty( medication.getCode().getCoding() ) ) {
			Coding coding = medication.getCode().getCoding().iterator().next();
			medication.getText().setStatus( Narrative.NarrativeStatus.GENERATED );
			medication.getText().setDivAsString( coding.getDisplay() );
		}
	}	
	
}
