package org.saintmartinhospital.legacy.data;

import org.saintmartinhospital.legacy.domain.DocType;
import org.saintmartinhospital.legacy.domain.GenderEnum;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.legacy.domain.PersonDoc;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.saintmartinhospital.legacy.domain.Medicine;
import org.saintmartinhospital.legacy.domain.MedicineSystemEnum;
import org.saintmartinhospital.legacy.domain.Prescription;
import org.saintmartinhospital.legacy.domain.PrescriptionStateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.saintmartinhospital.legacy.repository.PersonDAO;
import org.saintmartinhospital.legacy.repository.DocTypeRepository;
import org.saintmartinhospital.legacy.repository.MedicineRepository;
import org.saintmartinhospital.legacy.repository.PersonDocRepository;
import org.saintmartinhospital.legacy.repository.PrescriptionRepository;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InitialData {
	
	private static final String PROP_SEP = ",";
	private static final String PERSON_DOC_SEP = " ";
	private static final String PERSON_DOC_PROP_SEP = "\\|";
	private static final String PROP_ENUM_SEP = "_";
	
	private static enum DocTypePropEnum { ABREV, DESCRIPTION };
	private final List<Method> DOC_TYPE_METHODS = Arrays.asList( ReflectionUtils.getUniqueDeclaredMethods( DocType.class ) );	
	private static enum PersonPropEnum { FIRST_NAME, SECOND_NAME, LAST_NAME, BIRTHDATE, GENDER, EMAIL, NICK_NAME, DOCS };
	private final List<Method> PERSON_METHODS = Arrays.asList( ReflectionUtils.getUniqueDeclaredMethods( Person.class ) );
	private static enum MedicinePropEnum { SYSTEM, CODE, DESCRIPTION };
	private static final List<Method> MEDICINE_METHODS = Arrays.asList( ReflectionUtils.getUniqueDeclaredMethods( Medicine.class ) );
	private static enum PrescriptionPropEnum { DOC_TYPE, DOC_NUM, SYSTEM, CODE, STATE, DOSE_DESC, DOSE_FREQ_DAYS, DOSE_QTY };	
	
	@Autowired
	private DocTypeDataConfig docTypeConfig;
	@Autowired
	private PersonDataConfig personConfig;
	@Autowired
	private MedicineDataConfig medicineDataConfig;
	@Autowired
	private PrescriptionDataConfig prescriptionDataConfig;
	
	@Autowired
	private PersonDAO personDAO;
	@Autowired
	private DocTypeRepository docTypeRepository;
	@Autowired
	private PersonDocRepository personDocRepository;
	@Autowired
	private MedicineRepository medicineRepository;
	@Autowired
	private PrescriptionRepository prescriptionRespository;

	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final SimpleDateFormat BIRTHDATE_FORMAT = new SimpleDateFormat( DATE_FORMAT );

	
	@EventListener
	@Transactional
	public void populate( ApplicationReadyEvent event ) {
		Map<String,DocType> docTypeMap = populateDocTypes();
		populatePersons( docTypeMap );
		populateMedicines();
		populatePrescriptions();
	}
	
/*
 * private methods	
 */	
	
	@Transactional
	private Map<String,DocType> populateDocTypes() {
		Map<String,DocType> map = new HashMap<>();
		
		if( CollectionUtils.isNotEmpty( docTypeConfig.getList() ) )
			for( String curDocTypeAsString: docTypeConfig.getList() ) {
				DocType curDocType = saveDocType( curDocTypeAsString );
				map.put( curDocType.getAbrev(), curDocType );
			}
		
		return map;
	}
	
	@Transactional
	private DocType saveDocType( String docTypeAsString ) {
		Validate.notEmpty( docTypeAsString, "Unexpected empty doc type" );
		DocType docType = new DocType();

		String[] props = docTypeAsString.split( PROP_SEP );
		for( DocTypePropEnum curProp: DocTypePropEnum.values() ) {
			if( curProp.ordinal() >= props.length )
				throw new IllegalStateException( String.format( "Missing properties in %s", docTypeAsString ) );
			setEntityProp( docType, curProp.toString(), props[ curProp.ordinal() ], DOC_TYPE_METHODS );
		}
		docType.setCreateDate(  Calendar.getInstance() );
		
		return docTypeRepository.save( docType );
	}
	
	@Transactional
	private void populatePersons( Map<String,DocType> docTypeMap ) {
		if( CollectionUtils.isNotEmpty( personConfig.getList() ) )
			for( String curPersonAsString: personConfig.getList() )
				savePerson( curPersonAsString, docTypeMap );
	}
	
	@Transactional
	private void populateMedicines() {
		CollectionUtils.emptyIfNull( medicineDataConfig.getList() ).forEach( medicineAsString -> saveMedicine( medicineAsString ) );		
	}
	
	@Transactional
	private void populatePrescriptions() {
		CollectionUtils.emptyIfNull( prescriptionDataConfig.getList() ).forEach( prescriptionAsString -> savePrescription( prescriptionAsString ) );		
	}
	
	@Transactional
	private Person savePerson( String personAsString, Map<String,DocType> docTypeMap ) {
		Validate.notEmpty( personAsString, "Unexpected empty person" );
		Person person = new Person();
		List<String> personDocs = null;
		
		String[] props = personAsString.split( PROP_SEP );

		// Check that all expected properties are inside the person string
		if( props.length != PersonPropEnum.values().length )
			throw new IllegalStateException( String.format( "Missing property in \"%s\". Expected property order: \"%s\"", personAsString,
				StringUtils.join( PersonPropEnum.values(), StringUtils.SPACE ) ) );
		
		for( PersonPropEnum curProp: PersonPropEnum.values() ) {
			if( curProp.ordinal() >= props.length )
				throw new IllegalStateException( String.format( "Missing properties in %s", personAsString ) );
				
			String value = props[ curProp.ordinal() ];

			if( StringUtils.isNotEmpty( value ) )
				switch( curProp ) {
					case GENDER:
						try {
							person.setGender( GenderEnum.valueOf( value.toUpperCase() ) );
						} catch( IllegalArgumentException e ) {
							throw new IllegalStateException( String.format( "Unexpected gender %s", value ) );
						}
						break;
					case BIRTHDATE:
						try {
							Calendar birthdate = Calendar.getInstance();
							birthdate.setTime( BIRTHDATE_FORMAT.parse( value ) );
							person.setBirthDate( birthdate );
						} catch( ParseException e ) {
							throw new IllegalArgumentException( String.format( "Invalid date format %s, expected format %s", value, DATE_FORMAT ) );
						}
						break;
					case DOCS:
						personDocs = Arrays.asList( value.split( PERSON_DOC_SEP ) );
						break;
					default:
						setEntityProp( person, curProp.toString(), props[ curProp.ordinal() ], PERSON_METHODS );
						break;
				}
		}
		person.setCreateDate( Calendar.getInstance() );
		personDAO.save( person );
		
		if( CollectionUtils.isNotEmpty( personDocs ) )
			savePersonDocs( person, docTypeMap, personDocs );
		
		return person;
	}
	
	@Transactional
	private void savePersonDocs( Person person, Map<String,DocType> docTypeMap, List<String> personDocs ) {
		if( CollectionUtils.isNotEmpty( personDocs ) ) {
			Calendar now = Calendar.getInstance();
			
			for( String curPersonDoc: personDocs ) {
				String[] values = curPersonDoc.split( PERSON_DOC_PROP_SEP );
				String docAbrev = values[0];
				String docValue = values[1];

				Validate.notEmpty( docAbrev, "Unexpected empty person document abreviation in %s", curPersonDoc );
				Validate.notEmpty( docValue, "Unexpected empty person document value in %s", curPersonDoc );
				
				DocType docType = docTypeMap.get( docAbrev );
				Validate.notNull( docType, "Unknown document abreviation in %s", curPersonDoc );

				personDocRepository.save( new PersonDoc( person, docType, docValue, now ) );
			}
		}
	}
	
	@Transactional
	private void saveMedicine( String medicineAsString ) {
		Validate.notBlank( medicineAsString, "Unexpected empty medicine" );
		
		Medicine medicine = new Medicine();
		String[] props = medicineAsString.split( PROP_SEP );

		// Check that all expected properties are inside the persona string
		if( props.length != MedicinePropEnum.values().length )
			throw new IllegalStateException( String.format( "Missing property in \"%s\". Expected property order: \"%s\"", medicineAsString,
				StringUtils.join( MedicinePropEnum.values(), StringUtils.SPACE ) ) );
		
		Stream.of( MedicinePropEnum.values() ).forEach( propEnum -> {
			if( propEnum.ordinal() >= props.length )
				throw new IllegalStateException( String.format( "Missing properties in %s", medicineAsString ) );			
			
			String propValue = props[ propEnum.ordinal() ];			
			
			if( StringUtils.isNotEmpty( propValue ) )
				switch( propEnum ) {
					case SYSTEM:
						try {
							medicine.setSystem( MedicineSystemEnum.valueOf( propValue.toUpperCase() ) );
						} catch( IllegalArgumentException e ) {
							throw new IllegalStateException( String.format( "Unexpected medicine system %s, see %s", propValue, medicineAsString ) );
						}
						break;
					default:
						setEntityProp( medicine, propEnum.toString(), propValue, MEDICINE_METHODS );
						break;
				}
		});
		
		medicine.setCreateDate(Calendar.getInstance() );
		medicineRepository.save( medicine );
	}
	
	@Transactional
	private void savePrescription( String prescriptionAsString ) {
		Validate.notBlank( prescriptionAsString, "Unexpected empty prescription" );
		
		Prescription prescription = new Prescription();
		String[] props = prescriptionAsString.split( PROP_SEP );
		
		// Check that all expected properties are inside the persona string
		if( props.length != PrescriptionPropEnum.values().length )
			throw new IllegalStateException( String.format( "Missing property in \"%s\". Expected property order: \"%s\"", prescriptionAsString,
				StringUtils.join( PrescriptionPropEnum.values(), StringUtils.SPACE ) ) );		
		

		String docType = null;
		String docNum = null;
		MedicineSystemEnum medicineSystem = null;
		String medicineCode = null;		
		
		for( PrescriptionPropEnum propEnum: PrescriptionPropEnum.values() ) {
			if( propEnum.ordinal() >= props.length )
				throw new IllegalStateException( String.format( "Missing properties in %s", prescriptionAsString ) );

			String propValue = props[ propEnum.ordinal() ];
			
			if( StringUtils.isNotEmpty( propValue ) )
				try {
					switch( propEnum ) {
						case DOC_TYPE:
							docType = propValue;
							break;
						case DOC_NUM:
							docNum = propValue;
							break;
						case SYSTEM:
							medicineSystem = MedicineSystemEnum.valueOf( propValue.toUpperCase() );
							break;
						case CODE:
							medicineCode = propValue;
							break;
						case STATE:
							prescription.setState( PrescriptionStateEnum.valueOf( propValue.toUpperCase() ) );
							break;
						case DOSE_FREQ_DAYS:
							prescription.setDoseFreqDays( Integer.parseInt( propValue ) );
							break;
						case DOSE_QTY:
							prescription.setDoseQuantityMg(Float.parseFloat( propValue ) );
							break;
						case DOSE_DESC:
							prescription.setDoseDesc( propValue );
							break;
						default:
							throw new IllegalArgumentException( "Unexpected property" );
					}
				} catch( NumberFormatException e ) {
					throw new IllegalStateException( String.format( "Expected number in %s", prescriptionAsString ), e );
				} catch( IllegalArgumentException e ) {
					throw new IllegalStateException( String.format( "Unexpected propery value in %s", prescriptionAsString ), e );
				}
		}			

		// Find the medicine that corresponds to the prescription
		List<Medicine> medicines = medicineRepository.findBySystemCode( medicineSystem, medicineCode );
		if( CollectionUtils.isEmpty( medicines ) )
			throw new IllegalStateException( String.format( "Unable to find medicine with %s %s", medicineSystem, medicineCode ) );
		if( medicines.size() > 1 )
			throw new IllegalStateException( String.format( "Found %d medicines with %s %s", medicines.size(), medicineSystem, medicineCode ) );
		prescription.setMedicine( medicines.iterator().next() );

		// Find the peson that correspond to the prescription
		List<Person> persons = personDAO.findByDocument( docType, docNum );
		if( CollectionUtils.isEmpty( persons ) )
			throw new IllegalStateException( String.format( "Unable to find person with %s %s", docType, docNum ) );
		if( persons.size() > 1 )
			throw new IllegalStateException( String.format( "Found %d persons with %s %s", persons.size(), docType, docNum ) );
		prescription.setPerson( persons.iterator().next() );

		prescription.setCreateDate(Calendar.getInstance() );
		prescriptionRespository.save( prescription );
	}	
	
	private void setEntityProp( Serializable entity, String propName, Object propValue, List<Method> methods ) {
		Validate.notNull( entity );
		Validate.notEmpty( propName );
		Method setter = null;
		
		// find the prop setter
		for( Method curMethod: methods )
			if( curMethod.getName().equals( getSetterName( propName ) ) ) {
				setter = curMethod;
				break;
			}
		
		if( setter == null )
			throw new IllegalArgumentException( String.format( "Unexpected property %s for class %s", propName, entity.getClass() ) );
		
		// invode prop method
		ReflectionUtils.invokeMethod( setter, entity, propValue );
	}
	
	private String getSetterName( String propName ) {
		StringBuilder setter = new StringBuilder();
		propName = propName.toLowerCase();
		String[] parts = propName.split( PROP_ENUM_SEP );
		for( int i = 0; i < parts.length; ++i )
			setter.append( StringUtils.capitalize( parts[i] ) );
		
		return String.format( "set%s", setter.toString() );
	}

}
