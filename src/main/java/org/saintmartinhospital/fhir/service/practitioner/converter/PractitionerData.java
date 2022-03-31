package org.saintmartinhospital.fhir.service.practitioner.converter;

import java.util.Calendar;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PractitionerData {

  private final String name;
  private final String lastName;
  private final String docSystem;
  private final String docValue;
  private final String gender;
  private final Calendar birthDate;
  private final String email;

}
