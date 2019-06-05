package org.saintmartinhospital.fhir.common;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.InstantType;
import org.saintmartinhospital.fhir.common.converter.FhirResourceConverter;
import org.springframework.util.CollectionUtils;



public class ListBundleProvider<B extends IBaseResource, E extends Serializable> implements IBundleProvider {
	
	private final List<E> list;
	private final FhirResourceConverter<E,B> converter;
	private final InstantType searchTime = InstantType.withCurrentTime();
	private Integer preferredPageSize = null;
	
	
	public ListBundleProvider( List<E> list, FhirResourceConverter<E,B> converter ) {
		Validate.notNull( list );
		Validate.notNull( converter );
		this.list = list;
		this.converter = converter;
	}

	@Override
	public List<IBaseResource> getResources( int fromIndex, int toIndex ) {
		int end = Math.min( toIndex, list.size() );
		List<E> subList = list.subList( fromIndex, end );
		return CollectionUtils.isEmpty( subList )? null: (List<IBaseResource>) converter.convertToResource( subList );
	}
	
	public void setPreferredPageSize( int preferredPageSize ) {
		this.preferredPageSize = preferredPageSize;
	}

	@Override
	public Integer preferredPageSize() {
		return preferredPageSize;
	}

	@Override
	public Integer size() {
		return list.size();
	}

	@Override
	public IPrimitiveType<Date> getPublished() {
		return searchTime;
	}

	@Override
	public String getUuid() {
		return null;
	}
	
}
