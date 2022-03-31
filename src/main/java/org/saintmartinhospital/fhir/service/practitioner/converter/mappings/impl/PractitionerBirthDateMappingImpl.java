package org.saintmartinhospital.fhir.service.practitioner.converter.mappings.impl;

import java.util.Calendar;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.Practitioner;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.fhir.service.practitioner.converter.PractitionerData;
import org.springframework.stereotype.Component;
import org.saintmartinhospital.fhir.service.practitioner.converter.mappings.PractitionerBirthDateMapping;

@Component
public class PractitionerBirthDateMappingImpl implements PractitionerBirthDateMapping {

  @Override
  public void mapTo(Practitioner practitioner, Person person) {
    Validate.notNull(practitioner, "Unexpecte null practitioner");
    Validate.notNull(person, "Unexpected null person");
    addBirthDate(practitioner, person.getBirthDate());
  }

  @Override
  public void mapFrom(Practitioner practitioner, Person person) {
    Validate.notNull(practitioner, "Unexpecte null practitioner");
    Validate.notNull(person, "Unexpected null person");

    if (practitioner.getBirthDate() != null) {
      Calendar fechaNac = Calendar.getInstance();
      fechaNac.setTime(practitioner.getBirthDate());
      person.setBirthDate(fechaNac);
    }
  }

  @Override
  public void populate(Practitioner practitioner, PractitionerData practitionerData) {
    addBirthDate(practitioner, practitionerData.getBirthDate());
  }

  /*
   * private methods
   */

  private void addBirthDate(Practitioner practitioner, Calendar birthdate) {
    if (birthdate != null)
      practitioner.setBirthDate(birthdate.getTime());
  }

}
