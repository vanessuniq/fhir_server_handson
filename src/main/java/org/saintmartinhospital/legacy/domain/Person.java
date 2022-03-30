package org.saintmartinhospital.legacy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;


@Entity
@Table(name = "PERSON")
@Data @NoArgsConstructor()
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Person implements java.io.Serializable {

	@Id
	@SequenceGenerator( name = "seq", sequenceName = "person_seq", initialValue = 1, allocationSize = 1 )	
	@GeneratedValue( strategy = GenerationType.AUTO, generator = "seq" )
	@Column(name = "PRSN_ID", unique = true, nullable = false, precision = 10, scale = 0)
	private Integer id;

	@NonNull
	@Column(name = "PRSN_FIRST_NAME", nullable = false, length = 50)
	private String firstName;

	@EqualsAndHashCode.Exclude
	@Column(name = "PRSN_SECOND_NAME", length = 50)
	private String secondName;

	@Column(name = "PRSN_LAST_NAME", nullable = false, length = 50)
	private String lastName;

	@NonNull
	@Column(name = "PRSN_BIRTH_DATE", nullable = false, length = 7)
	private Calendar birthDate;

	@NonNull
	@Enumerated( EnumType.STRING )
	@Column(name = "PRSN_GENDER", length = 30)
	private GenderEnum gender;
	
	@NonNull
	@Column(name = "PRSN_EMAIL", length = 100)
	private String email;

	@EqualsAndHashCode.Exclude
	@Column(name = "PRSN_NICK_NAME", length = 50)
	private String nickName;

	@NonNull @EqualsAndHashCode.Exclude
	@Column(name = "PRSN_CREATE_DATE", nullable = false, length = 7)
	private Calendar createDate;

	@JsonManagedReference
	@EqualsAndHashCode.Exclude
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "person")
	private Set<PersonDoc> docs = new HashSet<PersonDoc>(0);
	
}
