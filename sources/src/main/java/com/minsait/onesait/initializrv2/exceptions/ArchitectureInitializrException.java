package com.minsait.onesait.initializrv2.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ArchitectureInitializrException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ArchitectureInitializrException(String msg) {
		super(msg);
	}
}
