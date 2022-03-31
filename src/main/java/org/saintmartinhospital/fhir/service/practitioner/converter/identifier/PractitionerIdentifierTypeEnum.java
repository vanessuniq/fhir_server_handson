package org.saintmartinhospital.fhir.service.practitioner.converter.identifier;

import lombok.Getter;

@Getter
public enum PractitionerIdentifierTypeEnum {
  PRACTITIONER_ID("practitioner-id"),
  NI("ni"),
  PP("pp"),
  NPI("npi");

  private final String name;

  PractitionerIdentifierTypeEnum(String name) {
    this.name = name;
  }

}
