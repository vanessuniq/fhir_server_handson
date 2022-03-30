package org.saintmartinhospital.fhir.service.patient.converter;

import java.util.Calendar;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PatientData {

  private final String name;
  private final String lastName;
  private final String docSystem;
  private final String docValue;
  private final String gender;
  private final Calendar birthDate;
  private final String email;

}
