package org.saintmartinhospital.fhir.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

/**
 * Servlet principal de HAPI FHIR.
 * 
 * @author julian.martinez
 */
@WebServlet( urlPatterns = { "/fhir/*" }, displayName = "FHIR Server", loadOnStartup = 1 )
public class FhirRestfulServlet extends RestfulServer {
	
	private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger( FhirRestfulServlet.class );
	private static final int MAX_QTY_PAGING_PROVIDERS = 10;
	private static final int DEFAULT_PAGE_SIZE = 10;
	private static final int MAX_PAGE_SIZE = 100;
	
	@Value("${hospital.url}")
	private String serverBaseUrl;
	

	public FhirRestfulServlet() {
		super( FhirContext.forR4() );
	}
	
	@Override
	protected void initialize() throws ServletException {
		LOGGER.debug( "fhir restful server configuration ... " );
		
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
//		FhirRestfulServletEnv fhirEnv = (FhirRestfulServletEnv) context.getBean( "fhirRestfulServletEnv" );
		
		// Set resource providers
		setResourceProviders( context.getBeansOfType( IResourceProvider.class ).values() );
		setDefaultResponseEncoding( EncodingEnum.JSON );
		
		// Configure the server's identity/web address
		setServerAddressStrategy( new HardcodedServerAddressStrategy( serverBaseUrl ) );
		
		// Set paging provider
		FifoMemoryPagingProvider pagingProvider = new FifoMemoryPagingProvider( MAX_QTY_PAGING_PROVIDERS );
		pagingProvider.setDefaultPageSize( DEFAULT_PAGE_SIZE );
		pagingProvider.setMaximumPageSize( MAX_PAGE_SIZE );
		setPagingProvider( pagingProvider );
		
		// Set validator
//		FederadorValidatorFactory validatorFactory = (FederadorValidatorFactory) context.getBean( "federadorValidatorFactory" );
//		FhirValidator fhirValidator = validatorFactory.build( getFhirContext() );
//		getFhirContext().setParserErrorHandler( new StrictErrorHandler() );
		
		// Register interceptor
//		registerInterceptor( (InterceptorAdapter) context.getBean( "authenticationInterceptor" ) );
//		registerInterceptor( (StatusCodeInterceptor) context.getBean( "statusCodeInterceptor" ) );
//		registerInterceptor( (ResponseScoreInterceptor) context.getBean( "responseScoreInterceptor" ) );
		
		// Fill fhir restful servlet environment
//		fhirEnv.setValidator( fhirValidator );
	}

}
