package com.minsait.onesait.initializrv2.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.DefaultGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.minsait.onesait.initializrv2.codegen.ArchitectureInitializr;
import com.minsait.onesait.initializrv2.data.ArchitectureOptions;
import com.minsait.onesait.initializrv2.exceptions.BadRequestJavaException;
import com.minsait.onesait.initializrv2.exceptions.DependencyNotFoundException;
import com.minsait.onesait.initializrv2.exceptions.InitializrException;
import com.minsait.onesait.initializrv2.services.FilesUtils;
import com.minsait.onesait.initializrv2.services.InitializrService;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InitializrServiceImpl implements InitializrService {

	private static final String OUT_PATH = "/tmp/tmpLib/{{UUID}}";

	@Autowired
	private FilesUtils fileUtils;

	private Map<String, String[]> libraries;

	@PostConstruct
	public void setup() {
		// read libraries

		try {
			libraries = new TreeMap<>();
			URL resource = getClass().getClassLoader()
					.getResource("InitializrConfiguration/LibrariesConfiguration.json");
			JsonNode jsonNode = JsonLoader.fromURL(resource);
			ObjectMapper mapper = new ObjectMapper();

			JsonNode arrayNode = jsonNode.get("optionals");

			List<ArchitectureOptions> res = mapper.convertValue(arrayNode,
					new TypeReference<List<ArchitectureOptions>>() {
					});

			for (ArchitectureOptions opt : res) {
				String[] info = new String[2];
				info[0] = opt.getDescription();
				info[1] = opt.getUrl();
				libraries.put(opt.getName(), info);
			}

		} catch (IOException e) {
			log.error("Error readin initial configuration: " + e.getMessage());
		}

	}

	@Override
	public byte[] generateProject(String name, List<String> libraries, String yml, String type, String javaVersion)
			throws DependencyNotFoundException, InitializrException, BadRequestJavaException {
		// generate temp out path
		String tempPath = OUT_PATH.replace("{{UUID}}", UUID.randomUUID().toString());
		// generate project in out path

		OpenAPI api;

		if (yml != null && !yml.equals("{}"))
			api = new OpenAPIV3Parser().readContents(yml).getOpenAPI();
		else {

			try {
				String defautlYml = new StringBuilder("InitializrConfiguration/default-").append(type)
						.append("YML.yaml").toString();
				System.out.println(defautlYml);
				String resource = StreamUtils.copyToString(
						(InputStream) getClass().getClassLoader().getResource(defautlYml).getContent(),
						StandardCharsets.UTF_8);
				api = new OpenAPIV3Parser().readContents(resource).getOpenAPI();
			} catch (IOException e) {
				throw new InitializrException("Can not read default OpenAPI yaml:" + e.getMessage());
			}
		}

		if (name == null || name.equals(""))
			name = api.getInfo().getTitle();

		if (javaVersion != null && !javaVersion.isEmpty()) {
			if (!(javaVersion.equals("11")) && !(javaVersion.equals("16")) && !(javaVersion.equals("8"))) {
				throw new BadRequestJavaException("Invalid java version. Must be: 8 or 11 or 16");
			}
		} else {
			javaVersion = "8";
		}

		ArchitectureInitializr builder = new ArchitectureInitializr(name, tempPath + "/sources", type, javaVersion);
		if (libraries != null)
			for (String lib : libraries) {
				builder.setOption(lib, true);
			}

		CodegenConfig config = builder.build();

		final ClientOptInput clientOptInput = new ClientOptInput();
		config.additionalProperties().put(type, true);
		clientOptInput.config(config);
		clientOptInput.openAPI(api);
		DefaultGenerator generator = new DefaultGenerator();
		generator.opts(clientOptInput).generate();

		// Zip outPath
		byte[] zipBytes = fileUtils.zipFolder(tempPath + "/sources");

		// Delete out path
		fileUtils.removeDir(tempPath);
		return zipBytes;

	}

	@Override
	public Map<String, String[]> getLibraries() {
		return libraries;
	}

}
