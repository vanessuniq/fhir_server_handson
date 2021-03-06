package org.saintmartinhospital.legacy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table( name = "DOC_TYPE", uniqueConstraints = @UniqueConstraint(columnNames="DCTP_ABREV") )
@Data @NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DocType implements java.io.Serializable {

	@Id
	@SequenceGenerator( name = "seq", sequenceName = "doc_type_seq", initialValue = 1, allocationSize = 1 )	
	@GeneratedValue( strategy = GenerationType.AUTO, generator = "seq" )
	@Column(name = "DCTP_ID", unique = true, nullable = false, precision = 3, scale = 0)
	private Short id;
	
	@Column(name = "DCTP_ABREV", nullable = false, length = 10)
	private String abrev;
	
	@EqualsAndHashCode.Exclude
	@Column(name = "DCTP_DESC", nullable = false, length = 100)
	private String description;
	
	@EqualsAndHashCode.Exclude
	@Column(name = "DCTP_CREATE_DATE", nullable = false, length = 7)
	private Calendar createDate;
	
	public DocType( String abrev ) {
		this.abrev = abrev;
	}
	
	public DocType( String abrev, String description, Calendar createDate ) {
		this( abrev );
		this.description = description;
		this.createDate = createDate;
	}
	
}
