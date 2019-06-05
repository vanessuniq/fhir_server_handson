package org.saintmartinhospital.business.service.impl;

import org.saintmartinhospital.business.domain.DocType;
import org.saintmartinhospital.business.repository.DocTypeRepository;
import org.saintmartinhospital.business.service.DocTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class DocTypeServiceImpl implements DocTypeService {
	
	@Autowired
	private DocTypeRepository docTypeRepo;

	@Transactional
	@Override
	public DocType findByAbrev( String abrev ) {
		return docTypeRepo.findByAbrev( abrev );
	}

}
