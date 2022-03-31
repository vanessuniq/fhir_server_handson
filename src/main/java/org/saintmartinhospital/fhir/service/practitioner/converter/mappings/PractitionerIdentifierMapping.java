package org.saintmartinhospital.fhir.service.practitioner.converter.mappings;

import org.hl7.fhir.r4.model.Reference;

public interface PractitionerIdentifierMapping extends PractitionerDataMapping, PractitionerPopulator {

  Reference buildPractitionerReference(Integer personaId);

  Integer getIdFromReference(Reference practitionerReference);

}
