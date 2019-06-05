package org.saintmartinhospital.business.service.impl;

import java.util.Calendar;
import org.saintmartinhospital.business.domain.DocType;
import org.saintmartinhospital.business.domain.PersonDoc;
import org.saintmartinhospital.business.repository.PersonDocRepository;
import org.saintmartinhospital.business.service.DocTypeService;
import org.saintmartinhospital.business.service.PersonDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class PersonDocServiceImpl implements PersonDocService {
	
	@Autowired
	private PersonDocRepository personDocRepo;
	@Autowired
	private DocTypeService docTypeService;

	@Override
	public PersonDoc save( PersonDoc personDoc ) {
		if( personDoc != null ) {
			DocType docType = docTypeService.findByAbrev( personDoc.getDocType().getAbrev() );
			if( docType == null )
				throw new IllegalArgumentException( String.format( "Document type %s not found", personDoc.getDocType().getAbrev() ) );
			
			personDoc.setDocType( docType );
			personDoc.setCreateDate( Calendar.getInstance() );
			personDoc = personDocRepo.save( personDoc );
		}
		return personDoc;
	}
	
}
