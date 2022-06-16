package com.minsait.onesait.initializrv2.services.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileDeleteStrategy;
import org.springframework.stereotype.Service;

import com.minsait.onesait.initializrv2.exceptions.InitializrException;
import com.minsait.onesait.initializrv2.services.FilesUtils;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;

@Service
@Slf4j
public class FilesUtilsImpl implements FilesUtils{

	public void generateFolder(String path) {
		File directory = new File(path);
		if (!directory.exists()) {
			if (directory.mkdirs()) {
				log.info("Directorio creado");
			} else {
				log.error("Error al crear directorio");
			}
		}
	}
	
	public boolean removeDir(String path) {
		try {
			
			File dir = new File(path);
			if (dir.isDirectory()) {
				File[] files = dir.listFiles();
				if (files != null && files.length > 0) {
					for (File aFile : files) {						
						FileDeleteStrategy.FORCE.delete(aFile);
					}
				}
				Files.delete(dir.toPath());
			} else {
				Files.delete(dir.toPath());
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return true;
	}
	
	public byte[] zipFolder(String folder) throws InitializrException {

		try {
			String zipPath = folder + "/generatedZip.zip";
			new ZipFile(zipPath).addFolder(new File(folder));
			return Files.readAllBytes(Paths.get(zipPath));

		} catch (IOException  e) {
			throw new InitializrException("Error during file compression: "+e.getMessage());
		}
	}
	
	
}
