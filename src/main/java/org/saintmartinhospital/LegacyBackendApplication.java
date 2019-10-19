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
@EnableJpaRepositories( "org.saintmartinhospital.legacy.repository" )
@EntityScan( basePackages = { "org.saintmartinhospital.legacy.domain" } )
@EnableTransactionManagement
@ServletComponentScan	// this enables the WebServlet annotation in our ResftulServlet
@Slf4j
public class LegacyBackendApplication {
	
	public static void main( String[] args ) {
		SpringApplication.run(LegacyBackendApplication.class, args );
	}

}
