package org.saintmartinhospital.fhir.resource;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.saintmartinhospital.fhir.common.FhirResourceUtils;
import org.saintmartinhospital.fhir.service.practitioner.PractitionerService;
import org.saintmartinhospital.fhir.service.practitioner.converter.identifier.PractitionerIdentifierTypeEnum;
import org.saintmartinhospital.fhir.service.practitioner.converter.identifier.PractitionerIdentifierTypeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

@Component
public class PractitionerResource implements IResourceProvider {
  private static final String RESOURCE_TYPE = "Practitioner";

  @Autowired
  private PractitionerService practitionerService;
  @Autowired
  private PractitionerIdentifierTypeManager typeManager;

  @Override
  public Class<Practitioner> getResourceType() {
    return Practitioner.class;
  }

  @Read()
  public Practitioner findById(@IdParam IdType id) {
    Integer practitionerId = FhirResourceUtils.getInteger(id);
    Practitioner practitioner = practitionerService.findById(practitionerId);
    if (practitioner == null) {
      throw new ResourceNotFoundException(String.format("Practitioner with identifier %s not found",
          typeManager.formatIdValue(PractitionerIdentifierTypeEnum.PRACTITIONER_ID, practitionerId)));
    }
    return practitioner;
  }

  @Search()
  public IBundleProvider findByCriteria(@OptionalParam(name = Practitioner.SP_NAME) StringParam name,
      @OptionalParam(name = Practitioner.SP_FAMILY) StringParam family,
      @OptionalParam(name = Practitioner.SP_IDENTIFIER) TokenParam doc,
      @OptionalParam(name = Practitioner.SP_GENDER) StringParam gender,
      @OptionalParam(name = "email") StringParam email) {
    try {
      String docSystem = doc == null || StringUtils.isEmpty(doc.getSystem()) ? null : doc.getSystem();
      String docValue = doc == null || StringUtils.isEmpty(doc.getValue()) ? null : doc.getValue();

      return practitionerService.findByCriteria(FhirResourceUtils.getString(name),
          FhirResourceUtils.getString(family), docSystem, docValue,
          FhirResourceUtils.getString(gender), FhirResourceUtils.getString(email));
    } catch (IllegalArgumentException e) {
      throw new InvalidRequestException(e.getMessage());
    }
  }
}
