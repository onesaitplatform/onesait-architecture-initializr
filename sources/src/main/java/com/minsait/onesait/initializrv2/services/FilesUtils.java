package com.minsait.onesait.initializrv2.services;

import com.minsait.onesait.initializrv2.exceptions.InitializrException;

public interface FilesUtils {
	
	public void generateFolder(String path);
	public boolean removeDir(String path);
	public byte[] zipFolder(String folder) throws InitializrException;
}
