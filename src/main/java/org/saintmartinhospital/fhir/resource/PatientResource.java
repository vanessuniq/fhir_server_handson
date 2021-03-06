package org.saintmartinhospital.fhir.resource;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.Calendar;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.saintmartinhospital.fhir.common.FhirResourceUtils;
import org.saintmartinhospital.fhir.service.patient.PatientService;
import org.saintmartinhospital.fhir.service.patient.converter.identifier.PatientIdentifierTypeEnum;
import org.saintmartinhospital.fhir.service.patient.converter.identifier.PatientIdentifierTypeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PatientResource implements IResourceProvider {

  private static final String RESOURCE_TYPE = "Patient";
  private static final String FAMILY = "family";

  @Autowired
  private PatientService patientService;
  @Autowired
  private PatientIdentifierTypeManager typeManager;

  @Override
  public Class<Patient> getResourceType() {
    return Patient.class;
  }

  @Read()
  public Patient findById(@IdParam IdType id) {
    Integer patientId = FhirResourceUtils.getInteger(id);
    Patient patient = patientService.findById(patientId);
    if (patient == null)
      throw new ResourceNotFoundException(String.format("Patient with identifier %s not found",
          typeManager.formatIdValue(PatientIdentifierTypeEnum.PATIENT_ID, patientId)));
    return patient;
  }

  @Search()
  public IBundleProvider findByCriteria(@OptionalParam(name = Patient.SP_NAME) StringParam name,
      @OptionalParam(name = Patient.SP_FAMILY) StringParam fathersFamily,
      @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam doc,
      @OptionalParam(name = Patient.SP_BIRTHDATE) DateParam birthdate,
      @OptionalParam(name = Patient.SP_GENDER) StringParam gender,
      @OptionalParam(name = "email") StringParam email) {
    try {
      String docSystem = doc == null || StringUtils.isEmpty(doc.getSystem()) ? null : doc.getSystem();
      String docValue = doc == null || StringUtils.isEmpty(doc.getValue()) ? null : doc.getValue();
      // String emailSystem = email == null || StringUtils.isEmpty(email.getSystem())
      // ? null : email.getSystem();
      // String emailValue = email == null || StringUtils.isEmpty(email.getValue()) ?
      // null : email.getValue();
      Calendar birthdateCal = null;
      if (birthdate != null) {
        birthdateCal = Calendar.getInstance();
        birthdateCal.setTime(birthdate.getValue());
      }

      return patientService.findByCriteria(FhirResourceUtils.getString(name),
          FhirResourceUtils.getString(fathersFamily), docSystem, docValue,
          birthdateCal, FhirResourceUtils.getString(gender), FhirResourceUtils.getString(email));
    } catch (IllegalArgumentException e) {
      throw new InvalidRequestException(e.getMessage());
    }
  }

  @Create
  public MethodOutcome create(@ResourceParam Patient patient) {
    MethodOutcome outcome = new MethodOutcome();
    outcome.setId(new IdType(RESOURCE_TYPE, patientService.create(patient).longValue()));
    return outcome;
  }

  // TODO: Micro-Assignment #J-1

}
