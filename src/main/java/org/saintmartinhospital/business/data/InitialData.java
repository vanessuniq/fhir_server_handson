package org.saintmartinhospital.business.data;

import org.saintmartinhospital.business.domain.DocType;
import org.saintmartinhospital.business.domain.GenderEnum;
import org.saintmartinhospital.business.domain.Person;
import org.saintmartinhospital.business.domain.PersonDoc;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.saintmartinhospital.business.repository.PersonDAO;
import org.saintmartinhospital.business.repository.DocTypeRepository;
import org.saintmartinhospital.business.repository.PersonDocRepository;

@Component
public class InitialData {
	
	private static final String PROP_SEP = ",";
	private static final String PERSON_DOC_SEP = " ";
	private static final String PERSON_DOC_PROP_SEP = "\\|";
	private static final String PROP_ENUM_SEP = "_";
	
	private static enum DocTypePropEnum { ABREV, DESCRIPTION };
	private final List<Method> DOC_TYPE_METHODS = Arrays.asList( ReflectionUtils.getUniqueDeclaredMethods( DocType.class ) );	
	private static enum PersonPropEnum { FIRST_NAME, SECOND_NAME, FATHERS_LAST_NAME, MOTHERS_LAST_NAME, BIRTHDATE, GENDER, EMAIL, NICK_NAME, DOCS };
	private final List<Method> PERSON_METHODS = Arrays.asList( ReflectionUtils.getUniqueDeclaredMethods( Person.class ) );
	
	@Autowired
	private DocTypeDataConfig docTypeConfig;
	@Autowired
	private PersonDataConfig personConfig;
	
	@Autowired
	private PersonDAO personDAO;
	@Autowired
	private DocTypeRepository docTypeRepository;
	@Autowired
	private PersonDocRepository personDocRepository;

	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final SimpleDateFormat BIRTHDATE_FORMAT = new SimpleDateFormat( DATE_FORMAT );

	
	@EventListener
	public void populate( ApplicationReadyEvent event ) {
		Map<String,DocType> docTypeMap = populateDocTypes();
		populatePersons( docTypeMap );
	}
	
/*
 * private methods	
 */	
	
	private Map<String,DocType> populateDocTypes() {
		Map<String,DocType> map = new HashMap<>();
		
		if( CollectionUtils.isNotEmpty( docTypeConfig.getList() ) )
			for( String curDocTypeAsString: docTypeConfig.getList() ) {
				DocType curDocType = saveDocType( curDocTypeAsString );
				map.put( curDocType.getAbrev(), curDocType );
			}
		
		return map;
	}
	
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
	
	private void populatePersons( Map<String,DocType> docTypeMap ) {
		if( CollectionUtils.isNotEmpty( personConfig.getList() ) )
			for( String curPersonAsString: personConfig.getList() )
				savePerson( curPersonAsString, docTypeMap );
	}
	
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
