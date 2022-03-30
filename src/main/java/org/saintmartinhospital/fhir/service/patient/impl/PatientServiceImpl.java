package org.saintmartinhospital.fhir.service.patient.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.Calendar;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.Patient;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.legacy.domain.PersonDoc;
import org.saintmartinhospital.legacy.service.PersonService;
import org.saintmartinhospital.legacy.service.bo.FindPersonByCriteriaBO;
import org.saintmartinhospital.fhir.common.ListBundleProvider;
import org.saintmartinhospital.fhir.service.patient.PatientService;
import org.saintmartinhospital.fhir.service.patient.converter.PatientConverter;
import org.saintmartinhospital.fhir.service.patient.converter.PatientData;
import org.saintmartinhospital.legacy.service.exceptions.PersonNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PatientServiceImpl implements PatientService {

  @Autowired
  private PersonService personService;
  @Autowired
  private PatientConverter converter;

  @Transactional
  @Override
  public Patient findById(Integer patientId) {
    return converter.convertToResource(personService.findById(patientId));
  }

  @Transactional
  @Override
  public IBundleProvider findByCriteria(String name, String family, String docSystem, String docValue,
      Calendar birthDate, String gender, String email) throws IllegalArgumentException {
    // Build a patient from the parameters and then get the person
    PatientData patientData = PatientData.builder().name(name).lastName(family).docSystem(docSystem).docValue(docValue)
        .birthDate(birthDate).gender(gender).email(email).build();
    Patient patient = converter.buildPatient(patientData);
    Person person = converter.convertToEntity(patient);

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
  public Integer create(Patient patient) {
    Validate.notNull(patient, "Unexpected null patient");
    try {
      Person person = personService.save(converter.convertToEntity(patient));
      patient = converter.convertToResource(person);
      return Integer.parseInt(patient.getId());
    } catch (NumberFormatException e) {
      throw new InvalidRequestException(String.format("Unexpected not a number identifier \"%s\"", patient.getId()));
    } catch (IllegalArgumentException e) {
      throw new InvalidRequestException(e.getMessage());
    }
  }

  // TODO: Micro-Assignment #J-1

}
