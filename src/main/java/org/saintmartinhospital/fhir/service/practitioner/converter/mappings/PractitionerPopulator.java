package org.saintmartinhospital.fhir.service.practitioner.converter.mappings;

import org.hl7.fhir.r4.model.Practitioner;
import org.saintmartinhospital.fhir.service.practitioner.converter.PractitionerData;

public interface PractitionerPopulator {

  void populate(Practitioner practitioner, PractitionerData practitionerData);

}
