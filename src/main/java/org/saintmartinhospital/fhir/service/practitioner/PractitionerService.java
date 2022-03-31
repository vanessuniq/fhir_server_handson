package org.saintmartinhospital.fhir.service.practitioner;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.r4.model.Practitioner;

public interface PractitionerService {

  Practitioner findById(Integer practitionerId);

  IBundleProvider findByCriteria(String name, String fathersFamily, String docSystem, String docValue,
      String gender, String email) throws IllegalArgumentException;

  Integer create(Practitioner practitioner);

}
