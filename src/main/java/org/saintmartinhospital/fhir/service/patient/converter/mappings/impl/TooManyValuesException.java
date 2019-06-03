package org.saintmartinhospital.fhir.service.patient.converter.mappings.impl;


public class TooManyValuesException extends Exception {

	/**
	 * Creates a new instance of <code>TooManyValuesException</code> without detail message.
	 */
	public TooManyValuesException() {
	}

	/**
	 * Constructs an instance of <code>TooManyValuesException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public TooManyValuesException(String msg) {
		super(msg);
	}
}
