package com.minsait.onesait.initializrv2.services;

import java.util.List;
import java.util.Map;

import com.minsait.onesait.initializrv2.exceptions.InitializrException;
import com.minsait.onesait.initializrv2.exceptions.ModuleNotFoundException;

public interface ModulesService {

	public byte[] generate(List<String> modules) throws ModuleNotFoundException, InitializrException;

	public void downloadFile(String gitId, String outFolder);

	public Map<String, String[]> getModulesDescriptions();

}
