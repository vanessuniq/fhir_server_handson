package org.saintmartinhospital.fhir.service.patient.converter.mappings.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;


@Component
public class FamilyExtensionManager {

	private static final String FAMILY_EXT_BASE_URL = "http://hl7.org/fhir/StructureDefinition/";
	private static final String FATHER_EXT_URL = FAMILY_EXT_BASE_URL + "humanname-fathers-family";
	private static final String MOTHER_EXT_URL = FAMILY_EXT_BASE_URL + "humanname-mothers-family";	

	@Getter
	public enum ParentEnum {
		FATHER( FATHER_EXT_URL ),
		MOTHER( MOTHER_EXT_URL );
		
		private String url;
		
		ParentEnum( String url ) {
			this.url = url;
		}
	};
	
	@Getter
	public static class Family {
		private String fathersFamily;
		private String mothersFamily;
		
		public void set( ParentEnum parentEnum, String lastName ) {
			if( parentEnum != null && StringUtils.isNotEmpty( lastName ) ) {
				if( ParentEnum.FATHER.equals( parentEnum ) )
					fathersFamily = lastName;
				else if( ParentEnum.MOTHER.equals( parentEnum ) )
					mothersFamily = lastName;
			}
		}
		
		public boolean isEmpty() { 
			return StringUtils.isEmpty( fathersFamily ) && StringUtils.isEmpty( mothersFamily );
		}
	}
	
	public Extension getExtension( ParentEnum parent, String lastName ) {
		Validate.notNull( parent, "Unexpected null parent" );
		Validate.notEmpty( lastName, "Unexpected empty family" );
		return new Extension( parent.getUrl(), new StringType( lastName ) );
	}
	
	public Family getFamily( HumanName hname ) throws TooManyValuesException {
		final Map<ParentEnum,List<String>> parentFound = new HashMap<>();
		final Family familyFound = new Family();
		
		CollectionUtils.emptyIfNull( hname.getFamilyElement().getExtension() ).forEach( extension -> {
			String family = ((StringType) extension.getValue()).getValueAsString();
			
			if( StringUtils.isNotEmpty( family ) ) {
				// Find the ParentEnum that the extension belongs to
				Optional<ParentEnum> optionalParentEnum = Arrays.stream( ParentEnum.values() ).filter( parentEnum -> extension.getUrl().equals( parentEnum.getUrl() ) ).findFirst();
				if( !Optional.empty().equals( optionalParentEnum ) ) {
					ParentEnum parentEnum = optionalParentEnum.get();

					// Initialize the entry in parentFound map if necessary
					if( parentFound.get( parentEnum ) == null )
						parentFound.put( parentEnum, new ArrayList<>() );

					// Collect the last name found in extension
					parentFound.get( parentEnum ).add( family );
				}
			}
		});
		
		// Get last names
		CollectionUtils.emptyIfNull( parentFound.keySet() ).forEach( parentEnum -> {
			List<String> lastNames = parentFound.get( parentEnum );
			if( CollectionUtils.isNotEmpty( lastNames ) && lastNames.size() > 1 )
				familyFound.set( parentEnum, lastNames.iterator().next() );
		});

		return familyFound;
	}
	
}
