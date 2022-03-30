package org.saintmartinhospital.legacy.domain;

import java.io.Serializable;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;


@Entity
@Table( name = "PRESCRIPTION" )
@Data @NoArgsConstructor
public class Prescription implements Serializable {

	@Id
	@SequenceGenerator( name = "seq", sequenceName = "prescription_seq", initialValue = 1, allocationSize = 1 )	
	@GeneratedValue( strategy = GenerationType.AUTO, generator = "seq" )	
	@Column(name = "PRES_ID", unique = true, nullable = false, precision = 10, scale = 0)
	private Integer id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PRES_PRSN_ID", nullable = false)
	private Person person;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PRES_MED_ID", nullable = false)
	private Medicine medicine;

	@NonNull
	@Column(name = "PRES_CREATE_DATE", nullable = false, length = 7)	
	private Calendar createDate;
	
	@NonNull
	@Enumerated( EnumType.STRING )
	@Column(name = "PRES_STATE", length = 30)		
	private PrescriptionStateEnum state;
	
	@Column(name = "PRES_DOSE_DESC", length = 500)
	private String doseDesc;
	
	@NonNull
	@Column(name = "PRES_DOSE_FREQ_DAYS", nullable = false, precision = 2, scale = 0)
	private Integer doseFreqDays;
	
	@NonNull
	@Column(name = "PRES_DOSE_QTY_MG", nullable = false, precision = 2, scale = 0)
	private Float doseQuantityMg;
	
}
