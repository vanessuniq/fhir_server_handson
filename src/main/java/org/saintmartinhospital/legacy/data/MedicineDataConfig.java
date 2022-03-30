package org.saintmartinhospital.legacy.data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("data.medicine")
public class MedicineDataConfig extends DataConfig {
}
