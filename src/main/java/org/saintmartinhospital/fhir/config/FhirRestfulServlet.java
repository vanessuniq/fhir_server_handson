package org.saintmartinhospital.fhir.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.IncomingRequestAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.conformance.ProfileUtilities;
import org.hl7.fhir.r4.context.IWorkerContext;
import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.hapi.validation.PrePopulatedValidationSupport;
import org.hl7.fhir.r4.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;


@WebServlet( urlPatterns = { "/fhir/*" }, displayName = "FHIR Server", loadOnStartup = 1 )
public class FhirRestfulServlet extends RestfulServer {
	
	private static final long serialVersionUID = 1L;
	private static final String PROFILES_DIRECTORY = "classpath:fhir/profiles/";
	private static final List<String> PROFILES = new ArrayList<>();
	static {
		PROFILES.add( "saintmartinhospital.patient.structuredefinition.xml" );
		PROFILES.add( "saintmartinhospital.medicationrequest.structuredefinition.xml" );		
	}

    private static final Logger LOGGER = LoggerFactory.getLogger( FhirRestfulServlet.class );
	private static final int MAX_QTY_PAGING_PROVIDERS = 10;
	private static final int DEFAULT_PAGE_SIZE = 5;
	private static final int MAX_PAGE_SIZE = 100;
	
	@Autowired
	private ResourceLoader resourceLoader;
	

	public FhirRestfulServlet() {
		super( FhirContext.forR4() );
	}
		
    @Override
	protected void initialize() throws ServletException {
		LOGGER.debug( "fhir restful server configuration ... " );
		
		ApplicationContext appContext = ApplicationContextProvider.getApplicationContext();
		
		// Set resource providers
		setResourceProviders( appContext.getBeansOfType( IResourceProvider.class ).values() );
		setDefaultResponseEncoding( EncodingEnum.JSON );
		
		// Configure the server's identity/web address
		setServerAddressStrategy( new IncomingRequestAddressStrategy() );
		
		// Set paging provider
		FifoMemoryPagingProvider pagingProvider = new FifoMemoryPagingProvider( MAX_QTY_PAGING_PROVIDERS );
		pagingProvider.setDefaultPageSize( DEFAULT_PAGE_SIZE );
		pagingProvider.setMaximumPageSize( MAX_PAGE_SIZE );
		setPagingProvider( pagingProvider );
				
		// Set validator
		FhirContext fhirContext = getFhirContext();
		FhirValidator validator = fhirContext.newValidator();
		FhirInstanceValidator instanceValidator = getInstanceValidator( fhirContext );
		validator.registerValidatorModule( instanceValidator );
		fhirContext.setParserErrorHandler( new StrictErrorHandler() );
		
		// Register a validator interceptor
		RequestValidatingInterceptor requestInterceptor = new RequestValidatingInterceptor();
		requestInterceptor.addValidatorModule( instanceValidator );
		requestInterceptor.setFailOnSeverity( ResultSeverityEnum.WARNING );
		requestInterceptor.setAddResponseHeaderOnSeverity( ResultSeverityEnum.INFORMATION );
		requestInterceptor.setResponseHeaderValue( "Validation on ${line}: ${message} ${severity}" );
		requestInterceptor.setResponseHeaderValueNoIssues( "No issues detected" );
		registerInterceptor( requestInterceptor );
	}
	
/*
 * private methods	
 */	
	
	private FhirInstanceValidator getInstanceValidator( FhirContext context ) {
		FhirInstanceValidator validator = null;
        
        // TODO: Micro-Assignment #J-4

		try {
			// Get the input stream of each profile
			List<InputStream> files = new ArrayList<>();
			for( String profile: PROFILES )
				files.add( resourceLoader.getResource( PROFILES_DIRECTORY + profile ).getInputStream() );
			
			if( CollectionUtils.isEmpty( files ) ) {
				// Create a default instance validator
				validator = new FhirInstanceValidator();
			} else {
				// Create an instance validator using the profiles
				PrePopulatedValidationSupport valSupport = new PrePopulatedValidationSupport();
				DefaultProfileValidationSupport defaultSupport = new DefaultProfileValidationSupport();
				ValidationSupportChain support = new ValidationSupportChain( valSupport, defaultSupport );
				loadStructureDefinitions( context, valSupport, defaultSupport, files );

				validator = new FhirInstanceValidator( support );
				validator.setAnyExtensionsAllowed( true );
			}
			
			return validator;
		} catch( IOException e ) {
			throw new IllegalStateException( String.format( "Can't find profiles [%s]", PROFILES_DIRECTORY ), e );
		}
	}
	
	private void loadStructureDefinitions( FhirContext context, PrePopulatedValidationSupport valSupport, DefaultProfileValidationSupport defaultSupport, List<InputStream> files ) {
		if( CollectionUtils.isNotEmpty( files ) ) {
			StructureDefinition derived, base;
			IParser parser = context.newXmlParser();
			
			IWorkerContext workerContext = new HapiWorkerContext( context, defaultSupport );
			ProfileUtilities profileUtilities = new ProfileUtilities( workerContext, new ArrayList<>(), null );
			
			for( InputStream file: files ) {
				try {
					// Create and prepare derived structure definition
					derived = parser.parseResource( StructureDefinition.class, new InputStreamReader( new BOMInputStream( file ) ) );
					base = defaultSupport.fetchStructureDefinition( context, derived.getBaseDefinition() );
					Validate.notNull( base );
					profileUtilities.generateSnapshot( base, derived, StringUtils.EMPTY, StringUtils.EMPTY );
					
					// Add derived definition to valSupport
					valSupport.addStructureDefinition( derived );
					
				} catch( FHIRException e ) {
					LOGGER.error( String.format( "Can't load profile", e ) );
				}
			}
		}
	}	
	

}


