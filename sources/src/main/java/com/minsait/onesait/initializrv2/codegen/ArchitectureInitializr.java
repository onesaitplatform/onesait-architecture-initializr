package com.minsait.onesait.initializrv2.codegen;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.SupportingFile;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.minsait.onesait.initializrv2.data.ArchitectureOptions;
import com.minsait.onesait.initializrv2.data.ArchitectureOptions.ArchitectureOptionsFiles;
import com.minsait.onesait.initializrv2.data.ArchitectureOptions.FileType;
import com.minsait.onesait.initializrv2.data.LibrariesConfiguration;
import com.minsait.onesait.initializrv2.exceptions.ArchitectureInitializrException;
import com.minsait.onesait.initializrv2.exceptions.DependencyNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArchitectureInitializr {

	private CodegenGenerator codegenGenerator;

	private LibrariesConfiguration librariesConfiguration;
	
	private String type;
	private String javaVersion;
	

	public ArchitectureInitializr(String moduleName, String path,String type, String javaVersion) {
		codegenGenerator = new CodegenGenerator();
		codegenGenerator.setModule(moduleName);
		codegenGenerator.setOutputDir(path);
		this.type=type;
		codegenGenerator.setLibrary(type);
		this.javaVersion=javaVersion;

		readSettings();
	}

	private void readSettings() {

		try {
			URL resource = getClass().getClassLoader()
					.getResource("InitializrConfiguration/LibrariesConfiguration.json");
			JsonNode jsonNode = JsonLoader.fromURL(resource);
			

			ObjectMapper mapper = new ObjectMapper();

			librariesConfiguration = mapper.treeToValue(jsonNode, LibrariesConfiguration.class);

			if (log.isTraceEnabled()) {
				log.trace("Loaded {} optionals libraries");
				for (ArchitectureOptions opts : librariesConfiguration.getOptionals()) {
					log.trace("Loaded {}: {}", opts.getName(), opts.getDescription());
				}
			}

		} catch (IOException e) {
			throw new ArchitectureInitializrException("Error loading configurations");
		}
	}

	public void setOption(String key, boolean enabled) throws DependencyNotFoundException {

		Optional<ArchitectureOptions> opt = librariesConfiguration.getOptionals().stream()
				.filter(e -> e.getName().equals(key)).findFirst();
		if (!opt.isPresent()) {
			throw new DependencyNotFoundException(HttpStatus.BAD_REQUEST,
					"Dependency {{KEY}} not found".replace("{{KEY}}", key));
		}

		opt.get().setDefaultValue(enabled);
	}

	public CodegenConfig build() {

		for (ArchitectureOptions entry : librariesConfiguration.getOptionals()) {
			codegenGenerator.additionalProperties().put(entry.getName(), entry.isDefaultValue());
			if (entry.isDefaultValue() && entry.getFiles() != null && !entry.getFiles().isEmpty()) {
				addFiles(entry.getFiles());
			}

		}
		//Architecture files
		addFiles(librariesConfiguration.getDefaults());
		
		if (javaVersion != null) {
			if(javaVersion.equals("11")) {
				codegenGenerator.setJava11(true);
			}
			if(javaVersion.equals("16")) {
				codegenGenerator.setJava16(true);
			}
		}
				

		//Test files
		addFiles(librariesConfiguration.getTests());

		return codegenGenerator;
	}

	private void addFiles(List<ArchitectureOptionsFiles> files) {
		for (ArchitectureOptionsFiles file : files) {
			if(!file.getScopes().contains(type))
				continue;
			String dest = "";
			StringBuilder mustachePath= new StringBuilder("architecture").append(File.separator) ;
			if (file.getType() == FileType.CLASS) {
				dest = (codegenGenerator.getSourceFolder() + File.separator + codegenGenerator.getBasePackage()
						+ File.separator + file.getDest()).replace(".", java.io.File.separator);
				mustachePath.append(file.getMustache());
			} else if (file.getType() == FileType.RESOURCE) {
				dest = ("src.main.resources" + file.getDest()).replace(".", java.io.File.separator);
				mustachePath.append(file.getMustache());
			} else if (file.getType() == FileType.FILE) {
				dest = file.getDest();
				mustachePath.append(file.getMustache());
			}else if(file.getType() == FileType.TEST) {
				dest=(codegenGenerator.getTestFolder()+ File.separator + codegenGenerator.getBasePackage()
				+ File.separator + file.getDest()).replace(".", java.io.File.separator);
				mustachePath.append("test").append(File.separator).append(file.getMustache());
			}

			codegenGenerator.supportingFiles().add(
					new SupportingFile(mustachePath.toString(), dest, file.getName()));
		}
	}

}
