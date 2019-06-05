package org.saintmartinhospital.business.service;

import org.saintmartinhospital.business.domain.DocType;


public interface DocTypeService {

	DocType findByAbrev( String abrev );
	
}
