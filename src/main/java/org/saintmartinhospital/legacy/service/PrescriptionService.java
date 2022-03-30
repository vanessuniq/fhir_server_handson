package org.saintmartinhospital.legacy.service;

import java.util.List;
import org.saintmartinhospital.legacy.domain.Prescription;
import org.saintmartinhospital.legacy.domain.PrescriptionStateEnum;
import org.saintmartinhospital.legacy.service.exceptions.MedicineNotFoundException;
import org.saintmartinhospital.legacy.service.exceptions.PersonNotFoundException;
import org.saintmartinhospital.legacy.service.exceptions.PrescriptionNotFoundException;


public interface PrescriptionService {

	Prescription findById( Integer id );
	Prescription save( Prescription prescription ) throws PersonNotFoundException, MedicineNotFoundException;
	List<Prescription> findByPersonState( Integer personaId, PrescriptionStateEnum estado );
	Prescription update( Prescription prescription ) throws PrescriptionNotFoundException, PersonNotFoundException, MedicineNotFoundException;
	
}
