package com.minsait.onesait.initializrv2.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Getter;

public class DependencyNotFoundException extends Exception{
	
	private static final long serialVersionUID = 1L;
	@Getter
	private final HttpStatus status;

	public DependencyNotFoundException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

}
