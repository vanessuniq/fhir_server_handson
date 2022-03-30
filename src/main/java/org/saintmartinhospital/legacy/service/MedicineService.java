package org.saintmartinhospital.legacy.service;

import java.util.List;
import org.saintmartinhospital.legacy.domain.Medicine;
import org.saintmartinhospital.legacy.service.exceptions.DataAlreadyExistsException;


public interface MedicineService {

	Medicine findById( Integer id );
	Medicine save( Medicine medicine ) throws DataAlreadyExistsException;
	List<Medicine> findLikeDescriptionIgnoreCase( String desc );
	
}
