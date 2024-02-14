package com.terralogic.hros.lms.exceptionHandling;

import lombok.Data;

@Data
public class ErrorMessage {
	
	private int code;
	private String message;
	private String reason;
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	@Override
	public String toString() {
		return "ErrorMessage [code=" + code + ", message=" + message + ", reason=" + reason + "]";
	}
	

}
