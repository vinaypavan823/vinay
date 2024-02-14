package com.terralogic.hros.lms.exceptionHandling;

import org.apache.logging.log4j.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;



import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandling {

	public static org.apache.logging.log4j.Logger logger = LogManager.getLogger(GlobalExceptionHandling.class);

	@ExceptionHandler(TranscodingException.class)
	@ResponseStatus(value = HttpStatus.CONFLICT)
	protected @ResponseBody ResponseEntity<ErrorMessage> handleInValidCredException(final TranscodingException ex, final HttpServletRequest request) {
		ErrorMessage error = getError(HttpStatus.CONFLICT, "Exception occured ", ex.getMessage());
		logger.error("Error {}", error);
		// return new ResponseEntity<>(error,HttpStatus.UNAUTHORIZED);
		return new ResponseEntity<>(error,HttpStatus.CONFLICT);
	}
	@ExceptionHandler(NoResourceFound.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	protected @ResponseBody ResponseEntity<ErrorMessage> handleNoResourceFoundException(final NoResourceFound ex) {
		ErrorMessage error = getError(HttpStatus.NOT_FOUND, "no resouce found", ex.getMessage());
		logger.error("Error {}", error);
		// return new ResponseEntity<>(error,HttpStatus.UNAUTHORIZED);
		return new ResponseEntity<>(error,HttpStatus.NOT_FOUND);
	}


	private ErrorMessage getError(HttpStatus httpStatus, String message, String reason) {
		ErrorMessage error = new ErrorMessage();
		error.setCode(httpStatus.value());
		error.setMessage(message);
		error.setReason(reason);
		return error;
	}

}
