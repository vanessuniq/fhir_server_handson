package org.saintmartinhospital.fhir.service.practitioner.converter.identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.Identifier;
import static org.saintmartinhospital.fhir.service.practitioner.converter.identifier.PractitionerIdentifierTypeEnum.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PractitionerIdentifierTypeManager {

  private final List<PractitionerIdentifierInfo> identifierInfo = new ArrayList<>();

  @Value("${fhir.identifier.patient.id.baseurl}")
  private String practitioner_id_baseurl;
  @Value("${fhir.identifier.patient.ni.baseurl}")
  private String ni_id_baseurl;
  @Value("${fhir.identifier.patient.ni.baseurl}")
  private String npi_id_baseurl;
  @Value("${fhir.identifier.patient.pp.baseurl}")
  private String pp_id_baseurl;

  @EventListener
  private void init(ApplicationReadyEvent event) {
    identifierInfo
        .add(new PractitionerIdentifierInfo(PRACTITIONER_ID, practitioner_id_baseurl, PRACTITIONER_ID.getName(),
            Identifier.IdentifierUse.OFFICIAL));
    identifierInfo.add(new PractitionerIdentifierInfo(PP, pp_id_baseurl, PP.getName(), Identifier.IdentifierUse.USUAL));
    identifierInfo.add(new PractitionerIdentifierInfo(NI, ni_id_baseurl, NI.getName(), Identifier.IdentifierUse.USUAL));
    identifierInfo
        .add(new PractitionerIdentifierInfo(NI, npi_id_baseurl, NPI.getName(), Identifier.IdentifierUse.USUAL));

  }

  public String formatIdValue(PractitionerIdentifierTypeEnum identifierType, Object value) {
    PractitionerIdentifierInfo info = findByIdentifierType(identifierType);
    Validate.notNull(info, "Unkown identifier %s for value %s", identifierType, value);
    return String.format("%s|%s", info.getUriTypeAsString(), value.toString());
  }

  public PractitionerIdentifierInfo findByUrl(final String url) {
    PractitionerIdentifierInfo idInfo = null;

    if (StringUtils.isNotEmpty(url)) {
      Optional<PractitionerIdentifierInfo> optionalInfo = identifierInfo.stream()
          .filter(info -> info.getUriTypeAsString().equals(url)).findFirst();
      if (!Optional.empty().equals(optionalInfo))
        idInfo = optionalInfo.get();
    }

    return idInfo;
  }

  public PractitionerIdentifierInfo findByIdentifierType(final PractitionerIdentifierTypeEnum identifierType) {
    PractitionerIdentifierInfo idInfo = null;

    if (identifierType != null) {
      Optional<PractitionerIdentifierInfo> optionalInfo = identifierInfo.stream()
          .filter(info -> info.getIdentifierType().equals(identifierType)).findFirst();
      if (!Optional.empty().equals(optionalInfo))
        idInfo = optionalInfo.get();
    }

    return idInfo;
  }

}
