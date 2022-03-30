package org.saintmartinhospital.legacy.service.exceptions;


public class DataAlreadyExistsException extends Exception {

	public DataAlreadyExistsException() {
	}

	public DataAlreadyExistsException( String msg ) {
		super( msg );
	}

	public DataAlreadyExistsException( String message, Throwable cause ) {
		super( message, cause);
	}

	public DataAlreadyExistsException( Throwable cause ) {
		super( cause );
	}
	
}
