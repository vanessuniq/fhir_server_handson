package org.saintmartinhospital.legacy.service;

import org.saintmartinhospital.legacy.domain.DocType;


public interface DocTypeService {

	DocType findByAbrev( String abrev );
	
}
