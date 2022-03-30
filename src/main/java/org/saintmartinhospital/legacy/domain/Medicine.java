package org.saintmartinhospital.legacy.domain;

import java.io.Serializable;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;


@Entity
@Table( name = "MEDICINE", uniqueConstraints = @UniqueConstraint( columnNames = { "MED_SYSTEM", "MED_CODE" } ) )
@Data @NoArgsConstructor()
public class Medicine implements Serializable {
	
	@Id
	@SequenceGenerator( name = "seq", sequenceName = "medicine_seq", initialValue = 1, allocationSize = 1 )	
	@GeneratedValue( strategy = GenerationType.AUTO, generator = "seq" )
	@Column(name = "MED_ID", unique = true, nullable = false, precision = 10, scale = 0)	
	private Integer id;
	
	@NonNull
	@Enumerated( EnumType.STRING )
	@Column(name = "MED_SYSTEM", length = 30)		
	private MedicineSystemEnum system;
	
	@NonNull
	@Column(name = "MED_CODE", nullable = false, length = 30)	
	private String code;
	
	@NonNull
	@Column(name = "MED_DESC", nullable = false, length = 128)
	private String description;
	
	@NonNull
	@Column(name = "MED_CREATE_DATE", nullable = false, length = 7)
	private Calendar createDate;

}
