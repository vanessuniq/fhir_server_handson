package org.saintmartinhospital.legacy.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table( name = "PERSON_DOC", uniqueConstraints = @UniqueConstraint(columnNames={"PRDT_PRSN_ID", "PRDT_DCTP_ID"}) )
@Data @NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PersonDoc implements java.io.Serializable {

	@Id
	@SequenceGenerator( name = "seq", sequenceName = "person_doc_seq", initialValue = 1, allocationSize = 1 )	
	@GeneratedValue( strategy = GenerationType.AUTO, generator = "seq" )	
	@Column(name = "PRDT_ID", unique = true, nullable = false, precision = 10, scale = 0)
	private Integer id;

	@JsonBackReference	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PRDT_PRSN_ID", nullable = false)
	private Person person;

	@JsonManagedReference
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PRDT_DCTP_ID", nullable = false)
	private DocType docType;
	
	@Column(name = "PRDT_DOC_VALUE", nullable = false, length = 50)	
	private String docValue;

	@EqualsAndHashCode.Exclude
	@Column(name = "PRDT_CREATE_DATE", nullable = false, length = 7)
	private Calendar createDate;

	@EqualsAndHashCode.Exclude
	@Column(name = "PRDT_DELETE_DATE", length = 7)
	private Calendar deleteDate;
	
	public PersonDoc( Person person, DocType docType, String docValue, Calendar createDate ) {
		this.person = person;
		this.docType = docType;
		this.docValue = docValue;
		this.createDate = createDate;
	}

}
