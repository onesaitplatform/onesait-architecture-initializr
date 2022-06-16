package com.minsait.onesait.initializrv2.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.architecture.audit.aop.Audit;
import com.minsait.onesait.initializrv2.exceptions.BadRequestJavaException;
import com.minsait.onesait.initializrv2.exceptions.DependencyNotFoundException;
import com.minsait.onesait.initializrv2.exceptions.InitializrException;
import com.minsait.onesait.initializrv2.services.InitializrService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Library Generator Controller", description = "Controller to generate a library with default archetype")
@RestController
@RequestMapping("/library")
@PreAuthorize("isAuthenticated()")
public class LibraryController {

	@Autowired
	private InitializrService initializrService;

	@PostMapping(value = "/generate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Operation(summary = "Generates a new project", description = "Generates a new project library with the name and libraries specified.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Request Successful, review the resulting object. If infoError is not null, then a functional error has occurred in the back-end "),
			@ApiResponse(responseCode = "400", description = "Bad request, review the request param"),
			@ApiResponse(responseCode = "401", description = "Unauthorized request."),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "404", description = "Resource not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected exception (Internal Server Error)") })
	@Audit
	public ResponseEntity<?> generateProject(
			@Parameter(description = "The name of the library to generate", example = "libraryName", required = true) @RequestParam(required = false) String name,
			@Parameter(description = "The dependencies to add to the pom") @RequestParam(required = false) List<String> libraries,
			@RequestHeader(required=false) String confluenceUser,
			String javaVersion) {
		byte[] zipBytes;
		if (name == null)
			return new ResponseEntity<>("Name  must be provided", HttpStatus.BAD_REQUEST);

		try {
			zipBytes = initializrService.generateProject(name, libraries, null, "lib-spring-boot",javaVersion);
		} catch (DependencyNotFoundException e) {
			return new ResponseEntity<>(e.getMessage(), e.getStatus());
		} catch (InitializrException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (BadRequestJavaException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		HttpHeaders h = new HttpHeaders();
		h.add("Content-Disposition", String.format("inline; filename=\"%s.zip\"", name));
		return new ResponseEntity<>(zipBytes, h, HttpStatus.OK);

	}

	@GetMapping(value = "/")
	@Operation(summary = "Get all libraries", description = "Get all the libraries availables for the creation of the new projects.")
	@Audit
	public ResponseEntity<Object> getAllLibraries(@RequestHeader(required=false) String confluenceUser) {

		Map<String, String[]> libs = initializrService.getLibraries();
		if (libs == null)
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		return new ResponseEntity<>(libs, HttpStatus.OK);

	}

}