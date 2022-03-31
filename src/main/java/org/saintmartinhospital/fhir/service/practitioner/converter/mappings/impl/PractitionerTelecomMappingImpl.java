package org.saintmartinhospital.fhir.service.practitioner.converter.mappings.impl;

import java.util.Collections;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Practitioner;
import org.saintmartinhospital.legacy.domain.Person;
import org.springframework.stereotype.Component;
import org.saintmartinhospital.fhir.service.practitioner.converter.PractitionerData;
import org.saintmartinhospital.fhir.service.practitioner.converter.mappings.PractitionerTelecomMapping;

@Component
public class PractitionerTelecomMappingImpl implements PractitionerTelecomMapping {

  @Override
  public void mapTo(Practitioner practitioner, Person person) {
    Validate.notNull(practitioner, "Unexpected null practitioner");
    Validate.notNull(person, "Unexpected null person");

    addEmail(practitioner, person.getEmail());
  }

  @Override
  public void mapFrom(Practitioner practitioner, Person person) {
    Validate.notNull(practitioner, "Unexpected null practitioner");
    Validate.notNull(person, "Unexpected null person");

    Optional<ContactPoint> optional = CollectionUtils.emptyIfNull(practitioner.getTelecom()).stream()
        .filter(contactPoint -> ContactPoint.ContactPointSystem.EMAIL.equals(contactPoint.getSystem())).findFirst();

    if (!Optional.empty().equals(optional))
      person.setEmail(optional.get().getValue());
  }

  @Override
  public void populate(Practitioner practitioner, PractitionerData practitionerData) {
    Validate.notNull(practitioner, "Unexpected null practitioner");
    Validate.notNull(practitionerData, "Unexpected null practitioner data");
    addEmail(practitioner, practitionerData.getEmail());
  }
  /*
   * private methods
   */

  private void addEmail(Practitioner practitioner, String email) {
    if (StringUtils.isNotEmpty(email)) {
      ContactPoint contact = new ContactPoint();
      contact.setSystem(ContactPoint.ContactPointSystem.EMAIL);
      contact.setValue(email);
      practitioner.setTelecom(Collections.singletonList(contact));
    }
  }

}
