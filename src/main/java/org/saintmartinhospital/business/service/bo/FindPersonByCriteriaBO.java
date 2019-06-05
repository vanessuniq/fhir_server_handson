package org.saintmartinhospital.business.service.bo;

import java.util.Calendar;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.saintmartinhospital.business.domain.GenderEnum;

@Getter
@Builder
public class FindPersonByCriteriaBO {
	
	private Integer id;
	private String name;
	private String lastName;
	private String docTypeAbrev;
	private String docValue;
	private GenderEnum gender;
	private Calendar birthDate;
	
	
	public static FindPersonByCriteriaBOBuilder builder() {
		return new CustomFindPersonByCriteriaBOBuilder();
	}
	
	private static class CustomFindPersonByCriteriaBOBuilder extends FindPersonByCriteriaBOBuilder {
		public FindPersonByCriteriaBO build() {
			Validate.isTrue( StringUtils.isNotBlank( super.name ) || StringUtils.isNotBlank( super.lastName ) || super.id != null ||
				( StringUtils.isNotBlank( super.docTypeAbrev ) && StringUtils.isNotBlank( super.docValue ) ), "Missing mandatory criteria property" );
			return super.build();
		}
	}

}
