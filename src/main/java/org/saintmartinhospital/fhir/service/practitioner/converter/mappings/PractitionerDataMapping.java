package org.saintmartinhospital.fhir.service.practitioner.converter.mappings;

import org.hl7.fhir.r4.model.Practitioner;
import org.saintmartinhospital.legacy.domain.Person;

public interface PractitionerDataMapping {

  // Map from person to practitioner
  void mapTo(Practitioner practitioner, Person person);

  // Map from practitioner to person
  void mapFrom(Practitioner practitioner, Person person);

}
