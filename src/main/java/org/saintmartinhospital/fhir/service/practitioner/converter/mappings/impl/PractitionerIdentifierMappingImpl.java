package org.saintmartinhospital.fhir.service.practitioner.converter.mappings.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.saintmartinhospital.legacy.domain.DocType;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.legacy.domain.PersonDoc;
import org.saintmartinhospital.fhir.service.practitioner.converter.PractitionerData;
import org.saintmartinhospital.fhir.service.practitioner.converter.mappings.PractitionerIdentifierMapping;
import org.saintmartinhospital.fhir.service.practitioner.converter.identifier.PractitionerIdentifierInfo;
import org.saintmartinhospital.fhir.service.practitioner.converter.identifier.PractitionerIdentifierTypeEnum;
import static org.saintmartinhospital.fhir.service.practitioner.converter.identifier.PractitionerIdentifierTypeEnum.*;
import org.saintmartinhospital.fhir.service.practitioner.converter.identifier.PractitionerIdentifierTypeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PractitionerIdentifierMappingImpl implements PractitionerIdentifierMapping {

  private static final String REFERENCE_PREFIX = "Practitioner/";

  @Autowired
  private PractitionerIdentifierTypeManager typeManager;

  @Transactional
  @Override
  public void mapTo(Practitioner practitioner, Person person) {
    Validate.notNull(practitioner, "Null practitioner");
    Validate.notNull(person, "Null person");

    // Add the practitioner-id identifier
    addIdentifier(practitioner, PRACTITIONER_ID, person.getId(), person.getCreateDate(), null);

    if (CollectionUtils.isNotEmpty(person.getDocs()))
      for (PersonDoc personDoc : person.getDocs()) {
        PractitionerIdentifierTypeEnum idType = PractitionerIdentifierTypeEnum
            .valueOf(personDoc.getDocType().getAbrev());
        if (idType == null)
          throw new IllegalArgumentException(
              String.format("Unexpected %s system identifier", personDoc.getDocType().getAbrev()));
        addIdentifier(practitioner, idType, personDoc.getDocValue(), personDoc.getCreateDate(),
            personDoc.getDeleteDate());
      }
  }

  @Override
  public void mapFrom(Practitioner practitioner, Person person) {
    if (CollectionUtils.isNotEmpty(practitioner.getIdentifier())) {
      person.setDocs(new HashSet<>());

      for (Identifier identifier : practitioner.getIdentifier()) {
        PractitionerIdentifierInfo idInfo = typeManager.findByUrl(identifier.getSystem());
        if (idInfo == null)
          throw new IllegalArgumentException(String.format("Unexpected system identifier %s", identifier.getSystem()));

        if (idInfo.getIdentifierType() == PractitionerIdentifierTypeEnum.PRACTITIONER_ID)
          try {
            Validate.isTrue(
                (StringUtils.isEmpty(practitioner.getId()) && StringUtils.isEmpty(identifier.getValue()))
                    || practitioner.getIdElement().getIdPart().equals(identifier.getValue()),
                "%s does not correspond to \"%s\"", practitioner.getId(),
                typeManager.formatIdValue(PractitionerIdentifierTypeEnum.PRACTITIONER_ID, identifier.getValue()));
            person.setId(Integer.parseInt(identifier.getValue()));
          } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                "The value of the system identifier \"%s\" can only be a number", idInfo.getUriTypeAsString()), e);
          }
        else {
          PersonDoc personDoc = new PersonDoc();
          personDoc.setDocType(new DocType(idInfo.getIdentifierType().toString()));
          personDoc.setDocValue(identifier.getValue());

          Period period = identifier.getPeriod();
          if (period != null) {
            if (period.getStart() != null && period.getEnd() != null)
              Validate.isTrue(period.getStart().before(period.getEnd()),
                  "The start date must occur before than the end date inside \"%s\" system identifier",
                  identifier.getSystem());
            personDoc.setCreateDate(toCalendar(period.getStart()));
            personDoc.setDeleteDate(toCalendar(period.getEnd()));
          }

          person.getDocs().add(personDoc);
        }
      }
    }
  }

  @Override
  public void populate(Practitioner practitioner, PractitionerData practitionerData) {
    Validate.notNull(practitioner, "Unexpected null practitioner");
    Validate.notNull(practitionerData, "Unexpected null practitioner data");

    if (StringUtils.isNotBlank(practitionerData.getDocSystem())
        && StringUtils.isNotBlank(practitionerData.getDocValue())) {
      PractitionerIdentifierInfo idInfo = typeManager.findByUrl(practitionerData.getDocSystem());
      if (idInfo == null)
        throw new IllegalArgumentException(
            String.format("The URI \"%s\" is not a valid system identifier", practitionerData.getDocSystem()));

      addIdentifier(practitioner, idInfo.getIdentifierType(), practitionerData.getDocValue(), null, null);
    }
  }

  @Override
  public Reference buildPractitionerReference(Integer personaId) {
    Reference reference = null;
    if (personaId != null) {
      reference = new Reference();
      Practitioner practitioner = new Practitioner();
      addIdentifier(practitioner, PRACTITIONER_ID, personaId, null, null);
      reference.setIdentifier(practitioner.getIdentifier().iterator().next());
      reference.setResource(practitioner);
    }
    return reference;
  }

  @Override
  public Integer getIdFromReference(Reference practitionerReference) {
    Integer id = null;
    if (practitionerReference != null) {
      try {
        String relativePath = practitionerReference.getReference();
        Validate.isTrue(StringUtils.isNotEmpty(relativePath), "Empty practitioner reference");
        Validate.isTrue(relativePath.indexOf(REFERENCE_PREFIX) == 0, "Wrong practitioner reference \"%s\"",
            relativePath);
        String idAsString = relativePath.substring(REFERENCE_PREFIX.length());
        id = Integer.parseInt(idAsString);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            String.format("The id \"%s\" is not valid practitioner identifier, number expected instead",
                practitionerReference.getId()));
      }
    }
    return id;
  }

  /*
   * private methods
   */

  private void addIdentifier(Practitioner practitioner, PractitionerIdentifierTypeEnum idType, Object value,
      Calendar create, Calendar delete) {
    if (practitioner != null && idType != null && value != null) {
      PractitionerIdentifierInfo idInfo = typeManager.findByIdentifierType(idType);
      if (idInfo == null)
        throw new IllegalStateException(String.format("Can't find information about %s identifier", idType));

      if (idInfo.getIdentifierType().equals(PRACTITIONER_ID))
        practitioner.setId(new IdType(value.toString()));

      Identifier identifier = practitioner.addIdentifier();
      identifier.setUse(idInfo.getUse());
      identifier.setSystemElement(idInfo.getUriType());
      identifier.setValue(value.toString());

      if (create != null) {
        Period period = new Period();
        period.setStart(create.getTime());
        if (delete != null)
          period.setEnd(delete.getTime());
        identifier.setPeriod(period);
      }
    }
  }

  private Calendar toCalendar(Date date) {
    Calendar cal = null;
    if (date != null) {
      cal = Calendar.getInstance();
      cal.setTime(date);
    }
    return cal;
  }

}
