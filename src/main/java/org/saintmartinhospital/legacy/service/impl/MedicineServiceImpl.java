package org.saintmartinhospital.legacy.service.impl;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.saintmartinhospital.legacy.domain.Medicine;
import org.saintmartinhospital.legacy.repository.MedicineRepository;
import org.saintmartinhospital.legacy.service.MedicineService;
import org.saintmartinhospital.legacy.service.exceptions.DataAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class MedicineServiceImpl implements MedicineService {

	@Autowired
	private MedicineRepository medicineRepo;
	
	@Transactional
	@Override
	public Medicine findById( Integer id ) {
		Optional<Medicine> optional = medicineRepo.findById( id );
		return Optional.empty().equals( optional )? null: optional.get();
	}

	@Transactional
	@Override
	public Medicine save( Medicine medicine ) throws DataAlreadyExistsException {
		if( medicine != null ) {
			// Verify that the medicine doen't exist
			List<Medicine> found = medicineRepo.findBySystemCode( medicine.getSystem(), medicine.getCode() );
			if( CollectionUtils.isNotEmpty( found ) )
				throw new DataAlreadyExistsException();
			
			medicine.setCreateDate( Calendar.getInstance() );
			medicineRepo.save( medicine );
		}
		return medicine;
	}

	@Override
	public List<Medicine> findLikeDescriptionIgnoreCase( String desc ) {
		return medicineRepo.findLikeDescriptionIgnoreCase( desc );
	}

}
