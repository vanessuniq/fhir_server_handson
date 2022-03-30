package org.saintmartinhospital.legacy.repository;

import java.util.List;
import org.saintmartinhospital.legacy.domain.Medicine;
import org.saintmartinhospital.legacy.domain.MedicineSystemEnum;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineRepository extends CrudRepository<Medicine,Integer> {

	@Query( "select m from Medicine m where lower( description ) like lower( concat( '%', :text, '%' ) )")
	List<Medicine> findLikeDescriptionIgnoreCase( @Param("text") String text );
	
	@Query( "select m from Medicine m where system = :system and code = :code" )
	List<Medicine> findBySystemCode( @Param("system") MedicineSystemEnum system, @Param("code") String code );
	
}
