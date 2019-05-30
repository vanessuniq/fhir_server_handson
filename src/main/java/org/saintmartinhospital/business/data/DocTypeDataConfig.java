package org.saintmartinhospital.business.data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("data.doctype")
public class DocTypeDataConfig extends DataConfig {
}
