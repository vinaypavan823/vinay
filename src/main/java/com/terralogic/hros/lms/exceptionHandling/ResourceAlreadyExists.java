package com.terralogic.hros.lms.exceptionHandling;

public class ResourceAlreadyExists extends RuntimeException {
	 /**
	 * 
	 */
	private static final long serialVersionUID = -8476913674546402306L;

	public ResourceAlreadyExists(String message) {
	        super(message);
	   }

}
