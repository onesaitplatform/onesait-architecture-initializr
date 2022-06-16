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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.architecture.audit.aop.Audit;
import com.minsait.onesait.initializrv2.exceptions.InitializrException;
import com.minsait.onesait.initializrv2.exceptions.ModuleNotFoundException;
import com.minsait.onesait.initializrv2.services.ModulesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Modules Controller", description = "Controller to download modules impementd by architecture departament")
@RequestMapping("/modules")
@RestController
@PreAuthorize("isAuthenticated()")
public class ModulesController {

	@Autowired
	private ModulesService modulesService;

	@GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Operation(summary = "Generates a new project", description = "Generates a zip with modules specified.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Request Successful, review the resulting object. If infoError is not null, then a functional error has occurred in the back-end "),
			@ApiResponse(responseCode = "400", description = "Bad request, review the request param"),
			@ApiResponse(responseCode = "401", description = "Unauthorized request."),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "404", description = "Resource not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected exception (Internal Server Error)") })
	@Audit
	public ResponseEntity<?> downloadModules(
			@Parameter(description = "The name of the zip where modules will be added", example = "architectureZip", required = false) @RequestParam(defaultValue = "architectureZip") String name,
			@Parameter(description = "The list of modules to download") @RequestParam List<String> modules,
			@RequestHeader(required=false) String confluenceUser) {
		byte[] zipBytes;
		try {
			zipBytes = modulesService.generate(modules);
		} catch (ModuleNotFoundException e) {
			return new ResponseEntity<>(e.getMessage(), e.getStatus());
		} catch (InitializrException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		HttpHeaders h = new HttpHeaders();
		h.add("Content-Disposition", String.format("inline; filename=\"%s.zip\"", name));
		return new ResponseEntity<>(zipBytes, h, HttpStatus.OK);
	}

	@GetMapping(value = "/")
	@Operation(summary = "Get all modules", description = "Get all the modules availables for download")
	@Audit
	public ResponseEntity<Object> getAllModules(@RequestHeader(required=false) String confluenceUser) {

		Map<String, String[]> libs = modulesService.getModulesDescriptions();
		if (libs == null)
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		return new ResponseEntity<>(libs, HttpStatus.OK);

	}

}