package org.saintmartinhospital.legacy.service.impl;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import org.saintmartinhospital.legacy.domain.Medicine;
import org.saintmartinhospital.legacy.domain.Person;
import org.saintmartinhospital.legacy.domain.Prescription;
import org.saintmartinhospital.legacy.domain.PrescriptionStateEnum;
import org.saintmartinhospital.legacy.repository.PrescriptionRepository;
import org.saintmartinhospital.legacy.service.MedicineService;
import org.saintmartinhospital.legacy.service.PersonService;
import org.saintmartinhospital.legacy.service.PrescriptionService;
import org.saintmartinhospital.legacy.service.exceptions.MedicineNotFoundException;
import org.saintmartinhospital.legacy.service.exceptions.PersonNotFoundException;
import org.saintmartinhospital.legacy.service.exceptions.PrescriptionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class PrescriptionServiceImpl implements PrescriptionService {

	@Autowired
	private PrescriptionRepository prescriptionRepository;
	@Autowired
	private PersonService personService;
	@Autowired
	private MedicineService medicineService;
	
	@Transactional
	@Override
	public Prescription findById( Integer id ) {
		Optional<Prescription> optional = prescriptionRepository.findById( id );
		return Optional.empty().equals( optional )? null: optional.get();
	}

	@Transactional
	@Override
	public Prescription save( Prescription prescription ) throws PersonNotFoundException, MedicineNotFoundException {
		if( prescription != null ) {
			// Check if the person exists
			Person person = personService.findById( prescription.getPerson().getId() );
			if( person == null )
				throw new PersonNotFoundException();
			// Check if the medicine exists
			Medicine medicine = medicineService.findById( prescription.getMedicine().getId() );
			if( medicine == null )
				throw new MedicineNotFoundException();
			
			prescription.setPerson( person );
			prescription.setMedicine( medicine );
			prescription.setCreateDate( Calendar.getInstance() );
			prescriptionRepository.save( prescription );
		}
		return prescription;
	}

	@Override
	public List<Prescription> findByPersonState( Integer personId, PrescriptionStateEnum state ) {
		return state == null? prescriptionRepository.findByPerson( personId ): prescriptionRepository.findByPersonState( personId, state );
	}

	@Transactional
	@Override
	public Prescription update( Prescription prescription ) throws PrescriptionNotFoundException, PersonNotFoundException, MedicineNotFoundException {
		Prescription result = null;
		if( prescription != null ) {
			// Check if prescription exists
			Optional<Prescription> optional = prescriptionRepository.findById( prescription.getId() );
			if( Optional.empty().equals( optional ) )
				throw new PrescriptionNotFoundException();
			Prescription prescriptionFound = optional.get();
			// Check if the person exists
			Person personFound = personService.findById( prescription.getPerson().getId() );
			if( personFound == null )
				throw new PersonNotFoundException();
			// Check if the medicine exists
			Medicine medicineFound = medicineService.findById( prescription.getMedicine().getId() );
			if( medicineFound == null )
				throw new MedicineNotFoundException();

			prescriptionFound.setPerson( personFound );
			prescriptionFound.setMedicine( medicineFound );
			prescriptionFound.setState( prescription.getState() );
			prescriptionFound.setDoseDesc( prescription.getDoseDesc() );
			prescriptionFound.setDoseFreqDays(prescription.getDoseFreqDays() );
			prescriptionFound.setDoseQuantityMg(prescription.getDoseQuantityMg() );
			
			result = prescriptionFound;
		}
		return result;
	}

}
