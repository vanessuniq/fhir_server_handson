package org.saintmartinhospital;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan( "org.saintmartinhospital" )
@EnableJpaRepositories( "org.saintmartinhospital.business.repository" )
@EntityScan( basePackages = { "org.saintmartinhospital.business.domain" } )
@EnableTransactionManagement
@ServletComponentScan	// this enables the WebServlet annotation in our ResftulServlet
@Slf4j
public class FhirServerDemoApplication {
	
	public static void main( String[] args ) {
		SpringApplication.run( FhirServerDemoApplication.class, args );
	}

}
