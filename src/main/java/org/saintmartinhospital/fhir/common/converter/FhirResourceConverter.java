package org.saintmartinhospital.fhir.common.converter;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * Interface that convert an entity to a FHIR resource and viceversa
 *
 * @param <A> entity
 * @param <B> FHIR resource
 */
public interface FhirResourceConverter<A extends Serializable, B extends IBaseResource> {

	B convertToResource( A object );
	List<B> convertToResource( Collection<A> objects );

	A convertToEntity( B fhirResource );
	List<A> convertToEntity( Collection<B> fhirResources );

}
