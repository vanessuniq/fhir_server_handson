package org.saintmartinhospital.fhir.service.practitioner.converter.mappings.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Practitioner;
import org.saintmartinhospital.legacy.domain.GenderEnum;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.fhir.service.practitioner.converter.PractitionerData;
import org.saintmartinhospital.fhir.service.practitioner.converter.mappings.PractitionerGenderMapping;
import org.springframework.stereotype.Component;

@Component
public class PractitionerGenderMappingImpl implements PractitionerGenderMapping {

  @Getter
  private static class GenderMap {
    private final GenderEnum personGender;
    private final AdministrativeGender practitionerGender;

    public GenderMap(GenderEnum personGender, AdministrativeGender practitionerGender) {
      this.personGender = personGender;
      this.practitionerGender = practitionerGender;
    }
  }

  private static final List<GenderMap> GENDER_LIST = new ArrayList<>();
  static {
    GENDER_LIST.add(new GenderMap(GenderEnum.MALE, AdministrativeGender.MALE));
    GENDER_LIST.add(new GenderMap(GenderEnum.FEMALE, AdministrativeGender.FEMALE));
    GENDER_LIST.add(new GenderMap(GenderEnum.OTHER, AdministrativeGender.OTHER));
  }

  @Override
  public void mapTo(Practitioner practitioner, Person person) {
    Validate.notNull(practitioner, "Unexpected null practitioner");
    Validate.notNull(person, "Unexpected null person");

    Optional<GenderMap> optional = GENDER_LIST.stream()
        .filter(genderMap -> genderMap.getPersonGender().equals(person.getGender())).findFirst();
    Validate.validState(!Optional.empty().equals(optional), "Unexpected %s person gender", person.getGender());
    practitioner.setGender(optional.get().getPractitionerGender());
  }

  @Override
  public void mapFrom(Practitioner practitioner, Person person) {
    Validate.notNull(practitioner, "Unexpected null practitioner");
    Validate.notNull(person, "Unexpected null person");

    if (practitioner.getGender() != null) {
      Optional<GenderMap> optional = GENDER_LIST.stream()
          .filter(genderMap -> genderMap.getPractitionerGender().equals(practitioner.getGender())).findFirst();
      Validate.validState(!Optional.empty().equals(optional), "Unexpected %s practitioner gender",
          practitioner.getGender());
      person.setGender(optional.get().getPersonGender());
    }
  }

  @Override
  public void populate(Practitioner practitioner, final PractitionerData practitionerData) {
    Validate.notNull(practitioner, "Unexpected null practitioner");
    Validate.notNull(practitionerData, "Unexpected null practitioner data");

    if (StringUtils.isNotBlank(practitionerData.getGender())) {
      Optional<AdministrativeGender> optionalGender = Arrays.asList(AdministrativeGender.values()).stream()
          .filter(gender -> practitionerData.getGender().equalsIgnoreCase(gender.toString())).findFirst();
      if (Optional.empty().equals(optionalGender))
        throw new IllegalArgumentException(String.format("Unexpected gender \"%s\"", practitionerData.getGender()));
      practitioner.setGender(optionalGender.get());
    }
  }

}
