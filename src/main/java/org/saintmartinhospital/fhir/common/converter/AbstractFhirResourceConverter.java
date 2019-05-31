package org.saintmartinhospital.fhir.common.converter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.transaction.annotation.Transactional;


public abstract class AbstractFhirResourceConverter<A extends Serializable,B extends IBaseResource> implements FhirResourceConverter<A,B> {

	@Transactional( readOnly = true )
	@Override
	public List<B> convertToResource( Collection<A> objects ) {
		List<B> results = new ArrayList<>();
		for( A curObject: objects )
			results.add( convertToResource( curObject ) );
		return results;
	}

	@Transactional( readOnly = true )
	@Override
	public List<A> convertToEntity( Collection<B> fhirResources ) {
		List<A> results = new ArrayList<>();
		for( B curFhirResource: fhirResources )
			results.add( convertToEntity( curFhirResource ) );
		return results;

	}
	
}
