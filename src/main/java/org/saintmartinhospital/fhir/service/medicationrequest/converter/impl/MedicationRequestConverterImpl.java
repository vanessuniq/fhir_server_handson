package org.saintmartinhospital.fhir.service.medicationrequest.converter.impl;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static java.util.stream.Collectors.toList;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Timing;
import org.hl7.fhir.r4.model.UriType;
import org.saintmartinhospital.fhir.common.converter.AbstractFhirResourceConverter;
import org.saintmartinhospital.fhir.service.medication.converter.MedicationConverter;
import org.saintmartinhospital.fhir.service.medicationrequest.converter.MedicationRequestConverter;
import org.saintmartinhospital.fhir.service.patient.converter.PatientConverter;
import org.saintmartinhospital.legacy.domain.Medicine;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.legacy.domain.Prescription;
import org.saintmartinhospital.legacy.domain.PrescriptionStateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class MedicationRequestConverterImpl extends AbstractFhirResourceConverter<Prescription,MedicationRequest> implements MedicationRequestConverter {

	private static final String MEDICATION_REQUEST_ID_NAME = "medication-request-id";
	private static final Integer DEFAULT_DOSAGE_PERIOD = 1;
	private static final Timing.UnitsOfTime DEFAULT_DOSAGE_PERIOD_UNIT = Timing.UnitsOfTime.D;
	private static final BooleanType DEFAULT_DOSAGE_AS_NEEDED = new BooleanType( true );
	private static final String DEFAULT_DOSAGE_QUANTITY_UNIT = "mg";
	private static final String DEFAULT_DOSAGE_QUANTITY_SYSTEM_UNIT = "http://unitsofmeasure.org";
	private static final String DEFAULT_DOSAGE_QUANTITY_CODE = "mg";
	

	@Autowired
	private PatientConverter patientConverter;
	@Autowired
	private MedicationConverter medicationConverter;
	@Value("${fhir.hospital.url}")
	private String medicationRequestIdBaseUrl;
	private UriType medicationRequestUriType;
	
	@Getter
	private static class StatusMap {
		private final PrescriptionStateEnum state;
		private final MedicationRequest.MedicationRequestStatus status;
		
		public StatusMap( PrescriptionStateEnum state, MedicationRequest.MedicationRequestStatus status ) {
			this.state = state;
			this.status = status;
		}
	}
	
	private static final List<StatusMap> STATUS_LIST = new ArrayList<>();
	static { 
		STATUS_LIST.add( new StatusMap( PrescriptionStateEnum.VALID, MedicationRequest.MedicationRequestStatus.ACTIVE ) );
		STATUS_LIST.add( new StatusMap( PrescriptionStateEnum.RAW, MedicationRequest.MedicationRequestStatus.DRAFT ) );
		STATUS_LIST.add( new StatusMap( PrescriptionStateEnum.DISRUPTED, MedicationRequest.MedicationRequestStatus.CANCELLED ) );
	}
	
	
	@EventListener
	protected void init( ApplicationReadyEvent event ) {
		medicationRequestUriType = new UriType( String.format( "%s%s", medicationRequestIdBaseUrl, MEDICATION_REQUEST_ID_NAME ) );
	}
	
	@Transactional( readOnly = true )
	@Override
	public MedicationRequest convertToResource( Prescription prescription ) {
		MedicationRequest medicationRequest = null;
		if( prescription != null ) {
			medicationRequest = new MedicationRequest();
			addIdentifiers( medicationRequest, prescription );
			addDoseAndRate( medicationRequest, prescription );
			medicationRequest.setAuthoredOn( prescription.getCreateDate().getTime() );
			medicationRequest.setSubject( patientConverter.buildPatientReference( prescription.getPerson().getId() ) );
			medicationRequest.setMedication( medicationConverter.buildMedicationReference( prescription.getMedicine().getId() ) );
			medicationRequest.setIntent( MedicationRequest.MedicationRequestIntent.ORDER );
			
			// Find the status that corresponds to the state
			Optional<StatusMap> optional = STATUS_LIST.stream().filter( statusMap -> statusMap.getState().equals( prescription.getState() ) ).findFirst();
			Validate.isTrue( !Optional.empty().equals( optional ), "Unexpected %s prescription state", prescription.getState() );
			medicationRequest.setStatus( optional.get().getStatus() );
			
			addNarrative( medicationRequest );
		}
		return medicationRequest;
	}

	@Transactional( readOnly = true )
	@Override
	public Prescription convertToEntity( MedicationRequest medicationRequest ) {
		Prescription prescription = null;
		if( medicationRequest != null ) {
			prescription = new Prescription();
			addId( prescription, medicationRequest );
			addDosis( prescription, medicationRequest );
			addPerson( prescription, medicationRequest );
			addMedicine( prescription, medicationRequest );
			
			// Find the state that corresponds to the status
			Optional<StatusMap> optional = STATUS_LIST.stream().filter( statusMap -> statusMap.getStatus().equals( medicationRequest.getStatus() ) ).findFirst();
			Validate.isTrue( !Optional.empty().equals( optional ), "Unexpected %s medication request status", medicationRequest.getStatus() );
			prescription.setState( optional.get().getState() );
		}
		return prescription;
	}
	
	@Override
	public PrescriptionStateEnum getState( String status ) {
		PrescriptionStateEnum state = null;
		
		if( StringUtils.isNotEmpty( status ) ) {
			Optional<StatusMap> optional;

			try {
				MedicationRequest.MedicationRequestStatus statusEnum = MedicationRequest.MedicationRequestStatus.valueOf( status.toUpperCase() );
				// Find the state that corresponds to the status
				optional = STATUS_LIST.stream().filter( statusMap -> statusMap.getStatus().equals( statusEnum ) ).findFirst();
			} catch( IllegalArgumentException e ) {
				String validStatus = StringUtils.join( STATUS_LIST.stream().map( statusMap -> statusMap.getStatus().toString().toLowerCase() ).collect( toList() ), ", " );
				throw new IllegalArgumentException( String.format( "Unexpected \"%s\" medication status, valid values are: %s", status, validStatus ), e );
			}

			Validate.isTrue( !Optional.empty().equals( optional ), "Unexpected \"%s\" medication status", status );
			state = optional.get().getState();
		}
		
		return state;
	}

/*
 * private methods	
 */	
	
	private void addIdentifiers( MedicationRequest medicationRequest, Prescription prescription ) {
		List<Identifier> identifiers = null;
		
		if( prescription.getId() != null ) {
			medicationRequest.setId( new IdType( prescription.getId().toString() ) );
			
			Identifier identifier = new Identifier();
			identifier.setValue( prescription.getId().toString() );
			identifier.setUse( Identifier.IdentifierUse.OFFICIAL );
			identifier.setSystemElement( medicationRequestUriType );
			
			Period period = new Period();
			period.setStart( prescription.getCreateDate().getTime() );
			identifier.setPeriod( period );
			
			identifiers = Collections.singletonList( identifier );
		}
		
		medicationRequest.setIdentifier( identifiers );
	}
	
	private void addId( Prescription prescription, MedicationRequest medicationRequest ) {
		if( CollectionUtils.isNotEmpty( medicationRequest.getIdentifier() ) ) {
			String medicationRequestUri = medicationRequestUriType.getValue();
			try {
				Identifier identifier = medicationRequest.getIdentifier().iterator().next();
				Validate.isTrue( medicationRequestUri.equals( identifier.getSystem() ), "Unexpected medication request system identifier %s", medicationRequestUri );
				prescription.setId( Integer.parseInt( identifier.getValue() ) );
				
				if( identifier.getPeriod() != null ) {
					Calendar createDate = Calendar.getInstance();
					createDate.setTime( identifier.getPeriod().getStart() );
					prescription.setCreateDate( createDate );
				}
			} catch( NumberFormatException e ) {
				throw new IllegalArgumentException( String.format( "The value of the system identifier\"%s\" can only be a number", medicationRequestUri ), e );
			}
		}
	}
	
	private void addDoseAndRate( MedicationRequest medicationRequest, Prescription prescription ) {
		// Map period
		Timing.TimingRepeatComponent repeat = new Timing.TimingRepeatComponent();
		repeat.setFrequency( prescription.getDoseFreqDays() );
		repeat.setPeriod( DEFAULT_DOSAGE_PERIOD );
		repeat.setPeriodUnit( DEFAULT_DOSAGE_PERIOD_UNIT );

		// Map quantity
		Dosage.DosageDoseAndRateComponent doseAndRate = new Dosage.DosageDoseAndRateComponent();
		Quantity quantity = new Quantity();
		quantity.setValue( prescription.getDoseQuantityMg());
		quantity.setUnit( DEFAULT_DOSAGE_QUANTITY_UNIT );
		quantity.setSystem( DEFAULT_DOSAGE_QUANTITY_SYSTEM_UNIT );
		quantity.setCode( DEFAULT_DOSAGE_QUANTITY_CODE );
		doseAndRate.setDose( quantity );

		// Create dosage
		Dosage dosage = new Dosage();
		dosage.setAsNeeded( DEFAULT_DOSAGE_AS_NEEDED );
		dosage.setText( prescription.getDoseDesc() );
		Timing timing = new Timing();
		timing.setRepeat( repeat );
		dosage.setTiming( timing );
		dosage.setDoseAndRate( Collections.singletonList( doseAndRate ) );
		
		// Asigno la dosis al medication request
		medicationRequest.setDosageInstruction( Collections.singletonList( dosage ) );		
	}
	
	private void addDosis( Prescription prescription, MedicationRequest medicationRequest ) {
		List<Dosage> dosages = medicationRequest.getDosageInstruction();
		Validate.isTrue( CollectionUtils.isNotEmpty( dosages ), "Empty dosage instruction" );
		Validate.isTrue( dosages.size() == 1, "Only one dosage instruction must be specified" );
		Dosage dosage = dosages.iterator().next();
		
		try {
			Timing timing = dosage.getTiming();
			Timing.TimingRepeatComponent repeat = timing.getRepeat();
			Validate.isTrue( repeat.getPeriod().intValue() == DEFAULT_DOSAGE_PERIOD, "The repeat period must be 1" );
			Validate.isTrue( repeat.getPeriodUnitElement().getCode().toLowerCase().equals( DEFAULT_DOSAGE_PERIOD_UNIT.toString().toLowerCase() ), "The repeat period unit must be \"%s\"", DEFAULT_DOSAGE_PERIOD_UNIT.toString().toLowerCase() );
			Integer frequency = repeat.getFrequency();
			Validate.isTrue( frequency > 0, "Unexpected \"%d\" frequency", frequency );
			prescription.setDoseFreqDays( frequency );
		} catch( NumberFormatException e ) {
			throw new IllegalArgumentException( "Invalid repeat period, expected an integer value" );
		}
			
		try {
			List<Dosage.DosageDoseAndRateComponent> doseAndRates = dosage.getDoseAndRate();
			Validate.isTrue( CollectionUtils.isNotEmpty( doseAndRates ), "Empty dose and rate" );
			Validate.isTrue( doseAndRates.size() == 1, "Only one dose and rate must be specified" );
			Dosage.DosageDoseAndRateComponent doseAndRate = doseAndRates.iterator().next();
			Quantity quantity = doseAndRate.getDoseQuantity();
			Validate.isTrue( quantity.getUnit().equals( DEFAULT_DOSAGE_QUANTITY_UNIT ),"The dose and rate unit must be \"%s\"", DEFAULT_DOSAGE_QUANTITY_UNIT );
			Validate.isTrue( quantity.getSystem().equals( DEFAULT_DOSAGE_QUANTITY_SYSTEM_UNIT ), "The dose and rate system must be \"%s\"", DEFAULT_DOSAGE_QUANTITY_SYSTEM_UNIT );
			Validate.isTrue( quantity.getCode().equals( DEFAULT_DOSAGE_QUANTITY_CODE ), "The dose and rate code must be \"%s\"", DEFAULT_DOSAGE_QUANTITY_CODE );
			prescription.setDoseQuantityMg( quantity.getValue().floatValue() );
		} catch( NumberFormatException e ) {
			throw new IllegalArgumentException( "Invalid dose and rate quantity value" );
		}
		
		prescription.setDoseDesc( dosage.getText() );
	}
	
	@Transactional
	private void addPerson( Prescription prescription, MedicationRequest medicationRequest ) {
		Integer patientId = patientConverter.getIdFromReference( medicationRequest.getSubject() );
		Person person = new Person();
		person.setId( patientId );
		prescription.setPerson( person );
	}
	
	@Transactional
	private void addMedicine( Prescription prescription, MedicationRequest medicationRequest ) {
		Integer medicineId = medicationConverter.getIdFromReference( medicationRequest.getMedicationReference() );
		Medicine medicine = new Medicine();
		medicine.setId( medicineId );
		prescription.setMedicine( medicine );
	}

	private void addNarrative( MedicationRequest medicationRequest ) {
		Identifier subjectIdentifier = medicationRequest.getSubject().getIdentifier();
		Identifier medicationIdentifier = medicationRequest.getMedicationReference().getIdentifier();
		List<Dosage> dosages = medicationRequest.getDosageInstruction();
		if( subjectIdentifier != null && medicationIdentifier != null && CollectionUtils.isNotEmpty( dosages ) ) {
			String subjectString = String.format( "%s|%s", subjectIdentifier.getSystem(), subjectIdentifier.getValue() );
			
			String medicationString = String.format( "%s|%s", medicationIdentifier.getSystem(), medicationIdentifier.getValue() );

			Dosage dosage = dosages.iterator().next();
			Dosage.DosageDoseAndRateComponent dose = dosage.getDoseAndRateFirstRep();
			String doseString = String.format( "quantity: %.2f %s, frequency: %d", dose.getDoseQuantity().getValue(), dose.getDoseQuantity().getUnit(), dosage.getTiming().getRepeat().getFrequency() );
						
			medicationRequest.getText().setStatus( Narrative.NarrativeStatus.GENERATED );
			medicationRequest.getText().setDivAsString( String.format( "%s - %s - %s", subjectString, medicationString, doseString ) );
		}
	}	
	
}
