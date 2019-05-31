package org.saintmartinhospital.fhir.service.patient.converter.identifier;

public enum PatientIdentifierTypeEnum {
	PATIENT_ID( "patient-id" ),
	NI( "ni" ),
	PP( "pp" );
	
	private final String name;
	
	PatientIdentifierTypeEnum( String name ) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
