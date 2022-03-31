package org.saintmartinhospital.fhir.service.practitioner.converter.mappings.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.StringType;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.fhir.service.practitioner.converter.PractitionerData;
import org.saintmartinhospital.fhir.service.practitioner.converter.mappings.PractitionerNameMapping;
import org.springframework.stereotype.Component;

@Component
public class PractitionerNameMappingImpl implements PractitionerNameMapping {

  @Override
  public void mapTo(Practitioner practitioner, Person person) {
    Validate.notNull(practitioner, "Unexpected null practitioner");
    Validate.notNull(person, "Unexpected null person");
    addOfficialName(practitioner, person.getFirstName(), person.getSecondName(), person.getLastName());
    addNickName(practitioner, person.getNickName());
  }

  @Override
  public void mapFrom(Practitioner practitioner, final Person person) {
    Validate.notNull(practitioner, "Unexpected null practitioner");
    Validate.notNull(person, "Unexpected null person");

    CollectionUtils.emptyIfNull(practitioner.getName()).forEach(hname -> {
      if (HumanName.NameUse.OFFICIAL == hname.getUse()) {
        setNames(person, hname);
        setLastName(person, hname);
      } else if (HumanName.NameUse.NICKNAME == hname.getUse()) {
        setNickName(person, hname);
      }
    });
  }

  @Override
  public void populate(Practitioner practitioner, PractitionerData practitionerData) {
    Validate.notNull(practitioner, "Unexpected null practitioner");
    Validate.notNull(practitionerData, "Unexpected null practitioner data");
    addOfficialName(practitioner, practitionerData.getName(), null, practitionerData.getLastName());
  }

  /*
   * private methods
   */

  private void addOfficialName(Practitioner practitioner, String firstName, String secondName, String lastName) {
    final HumanName hname = new HumanName();
    hname.setUse(HumanName.NameUse.OFFICIAL);

    StringBuilder textBuilder = new StringBuilder();

    // Set names
    final List<StringType> names = new ArrayList<>();

    Arrays.asList(firstName, secondName).stream().forEach(name -> {
      if (StringUtils.isNotBlank(name)) {
        textBuilder
            .append(String.format("%s%s", textBuilder.length() > 0 ? StringUtils.SPACE : StringUtils.EMPTY, name));
        names.add(new StringType(name));
      }
    });
    hname.setGiven(names);

    // Set last names
    hname.setFamily(lastName);

    textBuilder.append(String.format(" %s", lastName));
    hname.setText(textBuilder.toString());

    practitioner.addName(hname);
  }

  private void addNickName(Practitioner practitioner, String nickName) {
    if (StringUtils.isNotBlank(nickName)) {
      HumanName hname = new HumanName();
      hname.setUse(HumanName.NameUse.NICKNAME);
      hname.setGiven(Collections.singletonList(new StringType(nickName)));
      practitioner.addName(hname);
    }
  }

  private void setNames(Person person, HumanName hname) {
    List<StringType> given = hname.getGiven();
    if (CollectionUtils.isNotEmpty(given)) {
      Iterator<StringType> iterator = given.iterator();
      person.setFirstName(iterator.next().getValue());
      if (iterator.hasNext())
        person.setSecondName(iterator.next().getValue());
    }
  }

  private void setLastName(Person person, HumanName hname) {
    person.setLastName(hname.getFamily());
  }

  private void setNickName(Person person, HumanName hname) {
    List<StringType> given = hname.getGiven();
    if (CollectionUtils.isNotEmpty(given))
      person.setNickName(given.iterator().next().getValue());
  }

}
