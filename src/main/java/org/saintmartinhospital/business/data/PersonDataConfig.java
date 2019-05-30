package org.saintmartinhospital.business.data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("data.person")
public class PersonDataConfig extends DataConfig {
}
