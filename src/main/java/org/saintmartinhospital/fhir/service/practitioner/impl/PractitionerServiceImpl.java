package org.saintmartinhospital.fhir.service.practitioner.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.Calendar;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.Practitioner;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.legacy.domain.PersonDoc;
import org.saintmartinhospital.legacy.service.PersonService;
import org.saintmartinhospital.legacy.service.bo.FindPersonByCriteriaBO;
import org.saintmartinhospital.fhir.common.ListBundleProvider;
import org.saintmartinhospital.fhir.service.practitioner.PractitionerService;
import org.saintmartinhospital.fhir.service.practitioner.converter.PractitionerConverter;
import org.saintmartinhospital.fhir.service.practitioner.converter.PractitionerData;
import org.saintmartinhospital.legacy.service.exceptions.PersonNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PractitionerServiceImpl implements PractitionerService {

  @Autowired
  private PersonService personService;
  @Autowired
  private PractitionerConverter converter;

  @Transactional
  @Override
  public Practitioner findById(Integer practitionerId) {
    return converter.convertToResource(personService.findById(practitionerId));
  }

  @Transactional
  @Override
  public IBundleProvider findByCriteria(String name, String family, String docSystem, String docValue,
      String gender, String email) throws IllegalArgumentException {
    // Build a practitioner from the parameters and then get the person
    PractitionerData practitionerData = PractitionerData.builder().name(name).lastName(family).docSystem(docSystem)
        .docValue(docValue)
        .gender(gender).email(email).build();
    Practitioner practitioner = converter.buildPractitioner(practitionerData);
    Person person = converter.convertToEntity(practitioner);

    // Build the search criteria
    String personDocAbrev = null;
    String personDocValue = null;
    if (CollectionUtils.isNotEmpty(person.getDocs())) {
      PersonDoc personDoc = person.getDocs().iterator().next();
      personDocAbrev = personDoc.getDocType().getAbrev();
      personDocValue = personDoc.getDocValue();
    }

    FindPersonByCriteriaBO criteriaBO = FindPersonByCriteriaBO.builder().id(person.getId()).name(person.getFirstName())
        .lastName(person.getLastName()).docTypeAbrev(personDocAbrev).docValue(personDocValue)
        .birthDate(person.getBirthDate()).gender(person.getGender()).email(person.getEmail()).build();

    return new ListBundleProvider(personService.findByCriteria(criteriaBO), converter);
  }

  @Override
  public Integer create(Practitioner practitioner) {
    Validate.notNull(practitioner, "Unexpected null practitioner");
    try {
      Person person = personService.save(converter.convertToEntity(practitioner));
      practitioner = converter.convertToResource(person);
      return Integer.parseInt(practitioner.getId());
    } catch (NumberFormatException e) {
      throw new InvalidRequestException(
          String.format("Unexpected not a number identifier \"%s\"", practitioner.getId()));
    } catch (IllegalArgumentException e) {
      throw new InvalidRequestException(e.getMessage());
    }
  }

}
