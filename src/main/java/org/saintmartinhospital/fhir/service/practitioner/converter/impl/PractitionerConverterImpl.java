package org.saintmartinhospital.fhir.service.practitioner.converter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.legacy.service.PersonService;
import org.saintmartinhospital.fhir.common.converter.AbstractFhirResourceConverter;
import org.saintmartinhospital.fhir.service.practitioner.converter.PractitionerConverter;
import org.saintmartinhospital.fhir.service.practitioner.converter.PractitionerData;
import org.saintmartinhospital.fhir.service.practitioner.converter.mappings.PractitionerDataMapping;
import org.saintmartinhospital.fhir.service.practitioner.converter.mappings.PractitionerIdentifierMapping;
import org.saintmartinhospital.fhir.service.practitioner.converter.mappings.PractitionerPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PractitionerConverterImpl extends AbstractFhirResourceConverter<Person, Practitioner>
    implements PractitionerConverter {

  @Autowired
  private ApplicationContext context;
  @Autowired
  private PersonService personService;
  @Autowired
  private PractitionerIdentifierMapping practitionerIdentifierMapping;

  private List<PractitionerDataMapping> mappers;
  private List<PractitionerPopulator> populators;

  @PostConstruct
  private void init() {
    mappers = new ArrayList<>(context.getBeansOfType(PractitionerDataMapping.class).values());
    populators = new ArrayList<>(context.getBeansOfType(PractitionerPopulator.class).values());
  }

  @Transactional(readOnly = true)
  @Override
  public Practitioner convertToResource(final Person person) {
    Practitioner practitioner = null;
    if (person != null) {
      Practitioner result = new Practitioner();
      mappers.forEach(mapper -> mapper.mapTo(result, personService.attach(person)));
      addNarrative(result);
      practitioner = result;
    }
    return practitioner;
  }

  @Override
  public Person convertToEntity(final Practitioner practitioner) {
    Person person = null;
    if (practitioner != null) {
      Person result = new Person();
      mappers.forEach(mapper -> mapper.mapFrom(practitioner, result));
      person = result;
    }
    return person;
  }

  @Override
  public Practitioner buildPractitioner(final PractitionerData practitionerData) {
    Practitioner practitioner = null;
    if (practitionerData != null) {
      final Practitioner result = new Practitioner();
      populators.forEach(populator -> populator.populate(result, practitionerData));
      practitioner = result;
    }
    return practitioner;
  }

  @Override
  public Reference buildPractitionerReference(Integer personaId) {
    return practitionerIdentifierMapping.buildPractitionerReference(personaId);
  }

  @Override
  public Integer getIdFromReference(Reference practitionerReference) {
    return practitionerIdentifierMapping.getIdFromReference(practitionerReference);
  }

  /*
   * private methods
   */

  private void addNarrative(Practitioner practitioner) {
    if (CollectionUtils.isNotEmpty(practitioner.getName())) {
      Optional<HumanName> optional = practitioner.getName().stream()
          .filter(hname -> hname.getUse() == HumanName.NameUse.OFFICIAL).findFirst();
      if (!Optional.empty().equals(optional)) {
        HumanName name = optional.get();
        practitioner.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
        practitioner.getText().setDivAsString(
            String.format("%s %s", name.getGivenAsSingleString(), name.getFamilyElement().getValueNotNull()));
      }
    }
  }

}
