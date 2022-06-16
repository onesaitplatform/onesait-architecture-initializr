package com.minsait.onesait.initializrv2.services;

import java.util.List;
import java.util.Map;

import com.minsait.onesait.initializrv2.exceptions.BadRequestJavaException;
import com.minsait.onesait.initializrv2.exceptions.DependencyNotFoundException;
import com.minsait.onesait.initializrv2.exceptions.InitializrException;

public interface InitializrService {

	public byte[] generateProject(String name, List<String> libraries, String yml,String type, String javaVersion)
			throws DependencyNotFoundException, InitializrException, BadRequestJavaException;

	public Map<String, String[]> getLibraries();
	
	//public byte[] generatePythonProject(String name, List<String> libraries, String yml, String type) throws DependencyNotFoundException, InitializrException;

}
