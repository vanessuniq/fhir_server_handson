package org.saintmartinhospital.fhir.service.practitioner.converter;

import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.fhir.common.converter.FhirResourceConverter;

public interface PractitionerConverter extends FhirResourceConverter<Person, Practitioner> {

  Practitioner buildPractitioner(PractitionerData practitionerData);

  Reference buildPractitionerReference(Integer personaId);

  Integer getIdFromReference(Reference practitionerReference);

}
