package org.saintmartinhospital.legacy.service.bo;

import java.util.Calendar;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.saintmartinhospital.legacy.domain.GenderEnum;

@Getter
@Builder
public class FindPersonByCriteriaBO {
	
	private final Integer id;
	private final String name;
	private final String lastName;
	private final String docTypeAbrev;
	private final String docValue;
	private final GenderEnum gender;
	private final Calendar birthDate;
	
	
	public static FindPersonByCriteriaBOBuilder builder() {
		return new CustomFindPersonByCriteriaBOBuilder();
	}
	
	private static class CustomFindPersonByCriteriaBOBuilder extends FindPersonByCriteriaBOBuilder {
        @Override
		public FindPersonByCriteriaBO build() {
			Validate.isTrue( StringUtils.isNotBlank( super.name ) || StringUtils.isNotBlank( super.lastName ) || super.id != null ||
				( StringUtils.isNotBlank( super.docTypeAbrev ) && StringUtils.isNotBlank( super.docValue ) ), "Missing mandatory criteria property" );
			return super.build();
		}
	}

}
