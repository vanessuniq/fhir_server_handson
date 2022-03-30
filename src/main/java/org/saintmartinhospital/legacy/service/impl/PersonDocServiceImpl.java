package org.saintmartinhospital.legacy.service.impl;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static java.util.stream.Collectors.toList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.saintmartinhospital.legacy.domain.DocType;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.legacy.domain.PersonDoc;
import org.saintmartinhospital.legacy.repository.PersonDocRepository;
import org.saintmartinhospital.legacy.service.DocTypeService;
import org.saintmartinhospital.legacy.service.PersonDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class PersonDocServiceImpl implements PersonDocService {
	
	@Autowired
	private PersonDocRepository personDocRepo;
	@Autowired
	private DocTypeService docTypeService;

	
	@Transactional
	@Override
	public PersonDoc save( PersonDoc personDoc ) {
		if( personDoc != null ) {
			DocType docType = docTypeService.findByAbrev( personDoc.getDocType().getAbrev() );
			if( docType == null )
				throw new IllegalArgumentException( String.format( "Document type %s not found", personDoc.getDocType().getAbrev() ) );
			
			if( personDoc.getDeleteDate() != null )
				Validate.isTrue( personDoc.getCreateDate() != null && personDoc.getCreateDate().before( personDoc.getDeleteDate() ) );
			
			personDoc.setDocType( docType );
			if( personDoc.getCreateDate() == null )
				personDoc.setCreateDate( Calendar.getInstance() );
			
			personDoc = personDocRepo.save( personDoc );
		}
		return personDoc;
	}

	@Transactional
	@Override
	public Set<PersonDoc> updateDocs( Person person, Set<PersonDoc> newDocs ) {
		Set<PersonDoc> docsToReturn = null;
		if( person != null ) {
			docsToReturn = new HashSet<>();
			Set<PersonDoc> savedDocs = person.getDocs();
			
			// Process all newDocs
			if( CollectionUtils.isNotEmpty( newDocs ) )
				for( PersonDoc newDoc: newDocs ) {
					PersonDoc doc;
					if( newDoc.getDocType() != null && StringUtils.isNotEmpty( newDoc.getDocType().getAbrev() ) && StringUtils.isNotEmpty( newDoc.getDocValue() ) ) {
						// Business rule: a person should have at most one document of a specific each type
						// Find the saved doc that has the same doc type and update it, else save new doc
						Optional<PersonDoc> optional = savedDocs.stream().filter( savedDoc -> savedDoc.getDocType().getAbrev().equals( newDoc.getDocType().getAbrev() ) ).findFirst();
						if( Optional.empty().equals( optional ) )
							doc = newDoc;
						else {
							PersonDoc found = optional.get();
							found.setDocValue( newDoc.getDocValue() );
							found.setCreateDate( newDoc.getCreateDate() );
							found.setDeleteDate( newDoc.getDeleteDate() );
							doc = found;
						}
						save( doc );
						docsToReturn.add( doc );
					}
				}
			
			// Process savedDocs and find those that are not in newDocs => then set the deleteDate
			for( PersonDoc savedDoc: savedDocs ) {
				List<PersonDoc> found = docsToReturn.stream().filter( doc -> doc.getDocType().getAbrev().equals( savedDoc.getDocType().getAbrev() ) ).collect( toList() );
				if( CollectionUtils.isEmpty( found ) ) {
					savedDoc.setDeleteDate( Calendar.getInstance() );
					save( savedDoc );
					docsToReturn.add( savedDoc );
				}
			}
		}
		return docsToReturn;
	}
	
}
