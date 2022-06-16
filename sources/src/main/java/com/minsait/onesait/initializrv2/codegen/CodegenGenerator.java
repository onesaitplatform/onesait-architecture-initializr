package com.minsait.onesait.initializrv2.codegen;

import static org.openapitools.codegen.utils.StringUtils.camelize;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.openapitools.codegen.CliOption;
import org.openapitools.codegen.CodegenConstants;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.CodegenResponse;
import org.openapitools.codegen.CodegenSecurity;
import org.openapitools.codegen.CodegenType;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.languages.AbstractJavaCodegen;
import org.openapitools.codegen.languages.features.BeanValidationFeatures;
import org.openapitools.codegen.languages.features.OptionalFeatures;
import org.openapitools.codegen.languages.features.PerformBeanValidationFeatures;
import org.openapitools.codegen.meta.features.DocumentationFeature;
import org.openapitools.codegen.meta.features.GlobalFeature;
import org.openapitools.codegen.meta.features.ParameterFeature;
import org.openapitools.codegen.meta.features.SchemaSupportFeature;
import org.openapitools.codegen.meta.features.SecurityFeature;
import org.openapitools.codegen.meta.features.WireFormatFeature;
import org.openapitools.codegen.templating.mustache.SplitStringLambda;
import org.openapitools.codegen.templating.mustache.TrimWhitespaceLambda;
import org.openapitools.codegen.utils.URLPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samskivert.mustache.Mustache;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CodegenGenerator extends AbstractJavaCodegen
		implements BeanValidationFeatures, PerformBeanValidationFeatures, OptionalFeatures {

	private static final Logger LOGGER = LoggerFactory.getLogger(CodegenGenerator.class);

	public static final String ARCHITECTURE_RESOURCE_PATH = "architecture/";
	public static final String JSON = "JsonValue";

	public static final String TITLE = "title";
	public static final String SERVER_PORT = "serverPort";
	public static final String CONTEXT_PATH = "contextPath";
	public static final String CONFIG_PACKAGE = "configPackage";
	public static final String BASE_PACKAGE = "basePackage";
	public static final String INTERFACE_ONLY = "interfaceOnly";
	public static final String SINGLE_CONTENT_TYPES = "singleContentTypes";
	public static final String VIRTUAL_SERVICE = "virtualService";
	public static final String SKIP_DEFAULT_INTERFACE = "skipDefaultInterface";

	public static final String JAVA_11 = "java11";
	public static final String JAVA_16 = "java16";
	public static final String ASYNC = "async";
	public static final String RESPONSE_WRAPPER = "responseWrapper";
	public static final String USE_TAGS = "useTags";
	public static final String SPRING_MVC_LIBRARY = "spring-mvc";
	public static final String SPRING_BOOT = "spring-boot";
	public static final String SPRING_BOOT_LIB = "lib-spring-boot";
	public static final String SPRING_CLOUD_LIBRARY = "spring-cloud";
	public static final String IMPLICIT_HEADERS = "implicitHeaders";
	public static final String OPENAPI_DOCKET_CONFIG = "swaggerDocketConfig";
	public static final String API_FIRST = "apiFirst";
	public static final String HATEOAS = "hateoas";
	public static final String RETURN_SUCCESS_CODE = "returnSuccessCode";
	public static final String UNHANDLED_EXCEPTION_HANDLING = "unhandledException";

	public static final String OPEN_BRACE = "{";
	public static final String CLOSE_BRACE = "}";

	protected String titleSet = "OpenAPI Spring";
	protected String configPackage = "org.openapitools.configuration";
	protected String basePackage = "org.openapitools";
	protected boolean interfaceOnly = false;
	protected boolean delegateMethod = false;
	protected boolean singleContentTypes = false;
	protected boolean java11 = false;
	protected boolean java16 = false;
	protected boolean asyncSet = false;
	protected boolean skipDefaultInterface = true;
	protected boolean useTags = true;
	protected boolean useBeanValidation = true;
	protected boolean performBeanValidation = false;
	protected boolean implicitHeaders = false;
	protected boolean openapiDocketConfig = false;
	protected boolean apiFirst = false;
	protected boolean useOptional = false;
	protected boolean virtualService = false;
	protected boolean hateoasSet = false;
	protected boolean returnSuccessCode = false;
	protected boolean unhandledException = false;

	private Map<String, String> apiPaths = new HashMap<>();

	private Map<String, String> apiSuffixs = new HashMap<>();

	public CodegenGenerator() {
		super();

		modifyFeatureSet(features -> features.includeDocumentationFeatures(DocumentationFeature.Readme)
				.wireFormatFeatures(EnumSet.of(WireFormatFeature.JSON, WireFormatFeature.XML, WireFormatFeature.Custom))
				.securityFeatures(EnumSet.of(SecurityFeature.OAuth2_Implicit, SecurityFeature.OAuth2_AuthorizationCode,
						SecurityFeature.OAuth2_ClientCredentials, SecurityFeature.OAuth2_Password,
						SecurityFeature.ApiKey, SecurityFeature.BasicAuth))
				.excludeGlobalFeatures(GlobalFeature.Callbacks, GlobalFeature.LinkObjects,
						GlobalFeature.ParameterStyling)
				.includeGlobalFeatures(GlobalFeature.XMLStructureDefinitions)
				.includeSchemaSupportFeatures(SchemaSupportFeature.Polymorphism)
				.excludeParameterFeatures(ParameterFeature.Cookie));

		outputFolder = "generated-code/architectureSpring";
		embeddedTemplateDir = templateDir = "ArchitectureSpring";

		groupId = "com.minsait.onesait";

		// clioOptions default redefinition need to be updated
		updateOption(CodegenConstants.INVOKER_PACKAGE, this.getInvokerPackage());
		updateOption(CodegenConstants.ARTIFACT_ID, this.getArtifactId());
		updateOption(CodegenConstants.API_PACKAGE, apiPackage);
		updateOption(CodegenConstants.MODEL_PACKAGE, modelPackage);

		apiTestTemplateFiles.clear(); // TODO: add test template

		// spring uses the jackson lib
		additionalProperties.put("jackson", "true");
		additionalProperties.put("openbrace", OPEN_BRACE);
		additionalProperties.put("closebrace", CLOSE_BRACE);

		cliOptions.add(new CliOption(TITLE, "server title name or client service name").defaultValue(titleSet));
		cliOptions.add(new CliOption(CONFIG_PACKAGE, "configuration package for generated code")
				.defaultValue(this.getConfigPackage()));
		cliOptions.add(new CliOption(BASE_PACKAGE, "base package (invokerPackage) for generated code")
				.defaultValue(this.getBasePackage()));
		cliOptions.add(CliOption.newBoolean(INTERFACE_ONLY,
				"Whether to generate only API interface stubs without the server files.", interfaceOnly));
		cliOptions.add(CliOption.newBoolean(SINGLE_CONTENT_TYPES,
				"Whether to select only one produces/consumes content-type by operation.", singleContentTypes));
		updateJava8CliOptions();
		cliOptions.add(CliOption.newBoolean(SKIP_DEFAULT_INTERFACE,
				"Whether to generate default implementations for java8 interfaces", skipDefaultInterface));
		cliOptions.add(CliOption.newBoolean(ASYNC, "use async Callable controllers", asyncSet));
		cliOptions.add(new CliOption(RESPONSE_WRAPPER,
				"wrap the responses in given type (Future, Callable, CompletableFuture,ListenableFuture, DeferredResult, HystrixCommand, RxObservable, RxSingle or fully qualified type)"));
		cliOptions.add(CliOption.newBoolean(VIRTUAL_SERVICE,
				"Generates the virtual service. For more details refer - https://github.com/elan-venture/virtualan/wiki"));
		cliOptions.add(
				CliOption.newBoolean(USE_TAGS, "use tags for creating interface and controller classnames", useTags));
		cliOptions
				.add(CliOption.newBoolean(USE_BEANVALIDATION, "Use BeanValidation API annotations", useBeanValidation));
		cliOptions.add(CliOption.newBoolean(PERFORM_BEANVALIDATION,
				"Use Bean Validation Impl. to perform BeanValidation", performBeanValidation));
		cliOptions.add(CliOption.newBoolean(IMPLICIT_HEADERS,
				"Skip header parameters in the generated API methods using @ApiImplicitParams annotation.",
				implicitHeaders));
		cliOptions.add(CliOption.newBoolean(OPENAPI_DOCKET_CONFIG,
				"Generate Spring OpenAPI Docket configuration class.", openapiDocketConfig));
		cliOptions.add(CliOption.newBoolean(API_FIRST,
				"Generate the API from the OAI spec at server compile time (API first approach)", apiFirst));
		cliOptions
				.add(CliOption.newBoolean(USE_OPTIONAL, "Use Optional container for optional parameters", useOptional));
		cliOptions.add(
				CliOption.newBoolean(HATEOAS, "Use Spring HATEOAS library to allow adding HATEOAS links", hateoasSet));
		cliOptions
				.add(CliOption.newBoolean(RETURN_SUCCESS_CODE, "Generated server returns 2xx code", returnSuccessCode));
		cliOptions.add(CliOption.newBoolean(UNHANDLED_EXCEPTION_HANDLING,
				"Declare operation methods to throw a generic exception and allow unhandled exceptions (useful for Spring `@ControllerAdvice` directives).",
				unhandledException));

		supportedLibraries.put(SPRING_BOOT, "Spring-boot Server application using the SpringFox integration.");
		supportedLibraries.put(SPRING_BOOT_LIB, "Spring-boot Server library using the SpringFox integration.");
		setLibrary(SPRING_BOOT);
		
		CliOption library = new CliOption(CodegenConstants.LIBRARY, CodegenConstants.LIBRARY_DESC)
				.defaultValue(SPRING_BOOT);
		library.setEnum(supportedLibraries);
		cliOptions.add(library);

	}

	private void updateJava8CliOptions() {
		log.info("Update Java8 Cli Options");
	}

	@Override
	public CodegenType getTag() {
		return CodegenType.SERVER;
	}

	@Override
	public String getName() {
		return "architecture";
	}

	@Override
	public String getHelp() {
		return "Generates a Java SpringBoot Server application using the SpringFox integration.";
	}

	@Override
	public void processOpts() {

		List<Pair<String, String>> configOptions = additionalProperties.entrySet().stream()
				.filter(e -> !Arrays.asList(API_FIRST, "hideGenerationTimestamp").contains(e.getKey()))
				.filter(e -> cliOptions.stream().map(CliOption::getOpt).anyMatch(opt -> opt.equals(e.getKey())))
				.map(e -> Pair.of(e.getKey(), e.getValue().toString())).collect(Collectors.toList());
		additionalProperties.put("configOptions", configOptions);

		// Process java8 option before common java ones to change the default
		// dateLibrary to java8.
		LOGGER.info("----------------------------------");
		if (additionalProperties.containsKey(JAVA_11)) {
			LOGGER.info("has JAVA11");
			this.setJava11(Boolean.valueOf(additionalProperties.get(JAVA_11).toString()));
			additionalProperties.put(JAVA_11, java11);
		}
		if (additionalProperties.containsKey(JAVA_16)) {
			LOGGER.info("has JAVA16");
			this.setJava16(Boolean.valueOf(additionalProperties.get(JAVA_16).toString()));
			additionalProperties.put(JAVA_16, java16);
		}

		if (!additionalProperties.containsKey(BASE_PACKAGE)
				&& additionalProperties.containsKey(CodegenConstants.INVOKER_PACKAGE)) {
			// set invokerPackage as basePackage:
			this.setBasePackage((String) additionalProperties.get(CodegenConstants.INVOKER_PACKAGE));
			additionalProperties.put(BASE_PACKAGE, basePackage);
			LOGGER.info("Set base package to invoker package (%s)", basePackage);
		}

		super.processOpts();

		// clear model and api doc template as this codegen
		// does not support auto-generated markdown doc at the moment
		// TODO: add doc templates
		modelDocTemplateFiles.remove("model_doc.mustache");
		apiDocTemplateFiles.remove("api_doc.mustache");

		if (additionalProperties.containsKey(TITLE)) {
			this.setTitle((String) additionalProperties.get(TITLE));
		}

		if (additionalProperties.containsKey(CONFIG_PACKAGE)) {
			this.setConfigPackage((String) additionalProperties.get(CONFIG_PACKAGE));
		} else {
			additionalProperties.put(CONFIG_PACKAGE, configPackage);
		}

		if (additionalProperties.containsKey(BASE_PACKAGE)) {
			this.setBasePackage((String) additionalProperties.get(BASE_PACKAGE));
		} else {
			additionalProperties.put(BASE_PACKAGE, basePackage);
		}

		if (additionalProperties.containsKey(VIRTUAL_SERVICE)) {
			this.setVirtualService(Boolean.valueOf(additionalProperties.get(VIRTUAL_SERVICE).toString()));
		}

		if (additionalProperties.containsKey(INTERFACE_ONLY)) {
			this.setInterfaceOnly(Boolean.valueOf(additionalProperties.get(INTERFACE_ONLY).toString()));
		}

		if (additionalProperties.containsKey(SINGLE_CONTENT_TYPES)) {
			this.setSingleContentTypes(Boolean.valueOf(additionalProperties.get(SINGLE_CONTENT_TYPES).toString()));
		}

		if (additionalProperties.containsKey(SKIP_DEFAULT_INTERFACE)) {
			this.setSkipDefaultInterface(Boolean.valueOf(additionalProperties.get(SKIP_DEFAULT_INTERFACE).toString()));
		}

		if (additionalProperties.containsKey(ASYNC)) {
			this.setAsync(Boolean.valueOf(additionalProperties.get(ASYNC).toString()));
			// fix for issue/1164
			convertPropertyToBooleanAndWriteBack(ASYNC);
		}

		if (additionalProperties.containsKey(USE_TAGS)) {
			this.setUseTags(Boolean.valueOf(additionalProperties.get(USE_TAGS).toString()));
		}

		if (additionalProperties.containsKey(USE_BEANVALIDATION)) {
			this.setUseBeanValidation(convertPropertyToBoolean(USE_BEANVALIDATION));
		}
		writePropertyBack(USE_BEANVALIDATION, useBeanValidation);

		if (additionalProperties.containsKey(PERFORM_BEANVALIDATION)) {
			this.setPerformBeanValidation(convertPropertyToBoolean(PERFORM_BEANVALIDATION));
		}
		writePropertyBack(PERFORM_BEANVALIDATION, performBeanValidation);

		if (additionalProperties.containsKey(USE_OPTIONAL)) {
			this.setUseOptional(convertPropertyToBoolean(USE_OPTIONAL));
		}

		if (additionalProperties.containsKey(IMPLICIT_HEADERS)) {
			this.setImplicitHeaders(Boolean.valueOf(additionalProperties.get(IMPLICIT_HEADERS).toString()));
		}

		if (additionalProperties.containsKey(OPENAPI_DOCKET_CONFIG)) {
			this.setOpenapiDocketConfig(Boolean.valueOf(additionalProperties.get(OPENAPI_DOCKET_CONFIG).toString()));
		}

		if (additionalProperties.containsKey(API_FIRST)) {
			this.setApiFirst(Boolean.valueOf(additionalProperties.get(API_FIRST).toString()));
		}

		if (additionalProperties.containsKey(HATEOAS)) {
			this.setHateoas(Boolean.valueOf(additionalProperties.get(HATEOAS).toString()));
		}

		if (additionalProperties.containsKey(RETURN_SUCCESS_CODE)) {
			this.setReturnSuccessCode(Boolean.valueOf(additionalProperties.get(RETURN_SUCCESS_CODE).toString()));
		}

		if (additionalProperties.containsKey(UNHANDLED_EXCEPTION_HANDLING)) {
			this.setUnhandledException(
					Boolean.valueOf(additionalProperties.get(UNHANDLED_EXCEPTION_HANDLING).toString()));
		}
		additionalProperties.put(UNHANDLED_EXCEPTION_HANDLING, this.isUnhandledException());

		typeMapping.put("file", "Resource");
		importMapping.put("Resource", "org.springframework.core.io.Resource");

		if (useOptional) {
			writePropertyBack(USE_OPTIONAL, useOptional);
		}

		supportingFiles.add(new SupportingFile("pom.mustache", "", "pom.xml"));
		supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));

		apiTemplateFiles.put("serviceInterface.mustache", ".java");
		apiTemplateFiles.put("serviceImpl.mustache", ".java");

		apiPaths.put("api.mustache", apiFileFolder());
		apiPaths.put("serviceInterface.mustache", (outputFolder + File.separator + sourceFolder + File.separator
				+ basePackage + File.separator + "services").replace(".", java.io.File.separator));
		apiPaths.put("serviceImpl.mustache", (outputFolder + File.separator + sourceFolder + File.separator
				+ basePackage + File.separator + "services.impl").replace(".", java.io.File.separator));

		apiSuffixs.put("api.mustache", "RestController");
		apiSuffixs.put("serviceInterface.mustache", "Service");
		apiSuffixs.put("serviceImpl.mustache", "ServiceImpl");

		if (!this.interfaceOnly) {
			if (library.equals(SPRING_BOOT)) {
				supportingFiles.add(new SupportingFile("springApplication.mustache",
						(sourceFolder + File.separator + basePackage).replace(".", java.io.File.separator),
						"Application.java"));

				supportingFiles.add(new SupportingFile("application.mustache",
						("src.main.resources").replace(".", java.io.File.separator), "application.yml"));

				supportingFiles.add(new SupportingFile("application-docker.mustache",
						("src.main.resources").replace(".", java.io.File.separator), "application-docker.yml"));

				if (!this.apiFirst) {
					supportingFiles.add(new SupportingFile("openapiDocumentationConfig.mustache",
							(sourceFolder + File.separator + configPackage).replace(".", java.io.File.separator),
							"OpenAPIDocumentationConfig.java"));
				} else {
					supportingFiles.add(new SupportingFile("openapi.mustache",
							("src/main/resources").replace("/", java.io.File.separator), "openapi.yaml"));
				}
			}else if(library.equals(SPRING_BOOT_LIB)) {
				
			}
		} else if (this.openapiDocketConfig && !library.equals(SPRING_CLOUD_LIBRARY) && !this.apiFirst) {
			supportingFiles.add(new SupportingFile("openapiDocumentationConfig.mustache",
					(sourceFolder + File.separator + configPackage).replace(".", java.io.File.separator),
					"OpenAPIDocumentationConfig.java"));
		}

		if (this.apiFirst) {
			apiTemplateFiles.clear();
			modelTemplateFiles.clear();
		} else {
			apiDocTemplateFiles.clear();
		}

		if (this.java11) {
			additionalProperties.put("javaVersion", "11");
			additionalProperties.put("jdk11-default-interface", !this.skipDefaultInterface);
			additionalProperties.put("jdk11", true);
			additionalProperties.put("java11", this.java11);
		} else if(this.java16){
			additionalProperties.put("javaVersion", "16");
			additionalProperties.put("jdk16-default-interface", !this.skipDefaultInterface);
			additionalProperties.put("jdk16", true);
			additionalProperties.put("java16", this.java16);
		}		
			else {
			additionalProperties.put("javaVersion", "1.8");
			additionalProperties.put("jdk8-default-interface", !this.skipDefaultInterface);
			additionalProperties.put("jdk8", true);
		}

		if (parentOverridden) {
			additionalProperties.put("parentOverridden", true);
			additionalProperties.put("parentGroupId", parentGroupId);
			additionalProperties.put("parentArtifactId", parentArtifactId);
			additionalProperties.put("parentVersion", parentVersion);
		}

		
		

		if (this.asyncSet) {
			additionalProperties.put(RESPONSE_WRAPPER, "CompletableFuture");
		}

		if (!this.apiFirst) {
			additionalProperties.put("useSpringfox", true);
		}

		// add lambda for mustache templates
		additionalProperties.put("lambdaEscapeDoubleQuote", (Mustache.Lambda) (fragment, writer) -> writer
				.write(fragment.execute().replaceAll("\"", Matcher.quoteReplacement("\\\""))));
		additionalProperties.put("lambdaRemoveLineBreak",
				(Mustache.Lambda) (fragment, writer) -> writer.write(fragment.execute().replaceAll("\\r|\\n", "")));

		additionalProperties.put("lambdaTrimWhitespace", new TrimWhitespaceLambda());

		additionalProperties.put("lambdaSplitString", new SplitStringLambda());
	}

	@Override
	public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co,
			Map<String, List<CodegenOperation>> operations) {
		if ((library.equals(SPRING_BOOT) || library.equals(SPRING_MVC_LIBRARY)) && !useTags) {
			String basePath = resourcePath;
			if (basePath.startsWith("/")) {
				basePath = basePath.substring(1);
			}
			int pos = basePath.indexOf('/');
			if (pos > 0) {
				basePath = basePath.substring(0, pos);
			}

			if (basePath.equals("")) {
				basePath = "default";
			} else {
				co.subresourceOperation = !co.path.isEmpty();
			}
			List<CodegenOperation> opList = operations.computeIfAbsent(basePath, k -> new ArrayList<>());
			opList.add(co);
			co.baseName = basePath;
		} else {
			super.addOperationToGroup(tag, resourcePath, operation, co, operations);
		}
	}

	@Override
	public void preprocessOpenAPI(OpenAPI openAPI) {
		super.preprocessOpenAPI(openAPI);

		if (!additionalProperties.containsKey(TITLE)) {
			// From the title, compute a reasonable name for the package and the API
			String title = openAPI.getInfo().getTitle();

			// Drop any API suffix
			if (title != null) {
				title = title.trim().replace(" ", "-");
				if (title.toUpperCase(Locale.ROOT).endsWith("API")) {
					title = title.substring(0, title.length() - 3);
				}

				this.titleSet = camelize(sanitizeName(title), true);
			}
			additionalProperties.put(TITLE, this.titleSet);
		}

		if (!additionalProperties.containsKey(SERVER_PORT)) {
			URL url = URLPathUtils.getServerURL(openAPI, serverVariableOverrides());
			this.additionalProperties.put(SERVER_PORT, URLPathUtils.getPort(url, 8080));
		}

		if (!additionalProperties.containsKey(CONTEXT_PATH)) {
			URL url = URLPathUtils.getServerURL(openAPI, serverVariableOverrides());
			this.additionalProperties.put(CONTEXT_PATH, URLPathUtils.getPath(url, "/api"));
		}
		
		if(library.equals(SPRING_BOOT_LIB))
			openAPI.getPaths().clear();
		
		if (openAPI.getPaths() != null && !library.equals(SPRING_BOOT_LIB)) {
			for (String pathname : openAPI.getPaths().keySet()) {
				PathItem path = openAPI.getPaths().get(pathname);
				if (path.readOperations() != null) {
					for (Operation operation : path.readOperations()) {
						if (operation.getTags() != null) {
							List<Map<String, String>> tags = new ArrayList<>();
							for (String tag : operation.getTags()) {
								Map<String, String> value = new HashMap<>();
								value.put("tag", tag);
								value.put("hasMore", "true");
								tags.add(value);
							}
							if (!tags.isEmpty()) {
								tags.get(tags.size() - 1).remove("hasMore");
							}
							if (!operation.getTags().isEmpty()) {
								String tag = operation.getTags().get(0);
								operation.setTags(Arrays.asList(tag));
							}
							operation.addExtension("x-tags", tags);
						}
					}
				}
			}
		}
	}

	@Override
	public Map<String, Object> postProcessOperationsWithModels(Map<String, Object> objs, List<Object> allModels) {
		Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
		if (operations != null && !library.equals(SPRING_BOOT_LIB)) {
			List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
			for (final CodegenOperation operation : ops) {
				List<CodegenResponse> responses = operation.responses;
				if (responses != null) {
					for (final CodegenResponse resp : responses) {
						if ("0".equals(resp.code)) {
							resp.code = "200";
						}
						doDataTypeAssignment(resp.dataType, new DataTypeAssigner() {
							@Override
							public void setReturnType(final String returnType) {
								resp.dataType = returnType;
							}

							@Override
							public void setReturnContainer(final String returnContainer) {
								resp.containerType = returnContainer;
							}
						});
					}
				}

				doDataTypeAssignment(operation.returnType, new DataTypeAssigner() {

					@Override
					public void setReturnType(final String returnType) {
						operation.returnType = returnType;
					}

					@Override
					public void setReturnContainer(final String returnContainer) {
						operation.returnContainer = returnContainer;
					}
				});

				if (implicitHeaders) {
					removeHeadersFromAllParams(operation.allParams);
				}
			}
		}

		return objs;
	}

	private interface DataTypeAssigner {
		void setReturnType(String returnType);

		void setReturnContainer(String returnContainer);
	}

	/**
	 * @param returnType       The return type that needs to be converted
	 * @param dataTypeAssigner An object that will assign the data to the respective
	 *                         fields in the model.
	 */
	private void doDataTypeAssignment(String returnType, DataTypeAssigner dataTypeAssigner) {
		final String rt = returnType;
		if (rt == null) {
//			dataTypeAssigner.setReturnType("void");
		} else if (rt.startsWith("List")) {
			int end = rt.lastIndexOf('>');
			if (end > 0) {
				dataTypeAssigner.setReturnType(rt.substring("List<".length(), end).trim());
				dataTypeAssigner.setReturnContainer("List");
			}
		} else if (rt.startsWith("Map")) {
			int end = rt.lastIndexOf('>');
			if (end > 0) {
				dataTypeAssigner.setReturnType(rt.substring("Map<".length(), end).split(",", 2)[1].trim());
				dataTypeAssigner.setReturnContainer("Map");
			}
		} else if (rt.startsWith("Set")) {
			int end = rt.lastIndexOf('>');
			if (end > 0) {
				dataTypeAssigner.setReturnType(rt.substring("Set<".length(), end).trim());
				dataTypeAssigner.setReturnContainer("Set");
			}
		}
	}

	/**
	 * This method removes header parameters from the list of parameters and also
	 * corrects last allParams hasMore state.
	 *
	 * @param allParams list of all parameters
	 */
	private void removeHeadersFromAllParams(List<CodegenParameter> allParams) {
		if (allParams.isEmpty()) {
			return;
		}
		final ArrayList<CodegenParameter> copy = new ArrayList<>(allParams);
		allParams.clear();

		for (CodegenParameter p : copy) {
			if (!p.isHeaderParam) {
				allParams.add(p);
			}
		}
		if (!allParams.isEmpty()) {
			allParams.get(allParams.size() - 1).hasMore = false;
			
		}
	}

	@Override
	public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
		generateYAMLSpecFile(objs);
		if (library.equals(SPRING_CLOUD_LIBRARY)) {
			List<CodegenSecurity> authMethods = (List<CodegenSecurity>) objs.get("authMethods");
			if (authMethods != null) {
				for (CodegenSecurity authMethod : authMethods) {
					authMethod.name = camelize(sanitizeName(authMethod.name), true);
				}
			}
		}
		return objs;
	}

	@Override
	public String toApiName(String name) {
		if (name.length() == 0) {
			return "DefaultApi";
		}

		if (name.toLowerCase().endsWith("restcontroller")) {
			name = name.substring(0, name.length() - "restcontroller".length());
		} else if (name.toLowerCase().endsWith("controller")) {
			name = name.substring(0, name.length() - "controller".length());
		}

		name = sanitizeName(name);
		return camelize(name);
	}

	@Override
	public void setParameterExampleValue(CodegenParameter p) {
		String type = p.baseType;
		if (type == null) {
			type = p.dataType;
		}

		if ("File".equals(type)) {
			String example;

			if (p.defaultValue == null) {
				example = p.example;
			} else {
				example = p.defaultValue;
			}

			if (example == null) {
				example = "/path/to/file";
			}
			example = "new org.springframework.core.io.FileSystemResource(new java.io.File(\"" + escapeText(example)
					+ "\"))";
			p.example = example;
		} else {
			super.setParameterExampleValue(p);
		}
	}

	@Override
	public String apiFilename(String templateName, String tag) {
		String suffix = apiTemplateFiles().get(templateName);
		String path = apiPaths.get(templateName);

		if (tag.toLowerCase().endsWith("restcontroller")) {
			tag = tag.substring(0, tag.length() - "restcontroller".length());
		} else if (tag.toLowerCase().endsWith("controller")) {
			tag = tag.substring(0, tag.length() - "controller".length());
		}

		tag = tag.concat(apiSuffixs.get(templateName));

		return path + File.separator + tag + suffix;
	}

	public void setTitle(String title) {
		this.titleSet = title;
	}

	public void setConfigPackage(String configPackage) {
		this.configPackage = configPackage;
	}

	public String getConfigPackage() {
		return this.configPackage;
	}

	public boolean isUnhandledException() {
		return unhandledException;
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

	public String getBasePackage() {
		return this.basePackage;
	}

	public void setInterfaceOnly(boolean interfaceOnly) {
		this.interfaceOnly = interfaceOnly;
	}

	public void setSingleContentTypes(boolean singleContentTypes) {
		this.singleContentTypes = singleContentTypes;
	}

	public void setSkipDefaultInterface(boolean skipDefaultInterface) {
		this.skipDefaultInterface = skipDefaultInterface;
	}

	public void setJava11(boolean java11) {
		this.java11 = java11;
	}
	
	public void setJava16(boolean java16) {
		this.java16 = java16;
	}

	public void setVirtualService(boolean virtualService) {
		this.virtualService = virtualService;
	}

	public void setAsync(boolean async) {
		this.asyncSet = async;
	}

	public void setUseTags(boolean useTags) {
		this.useTags = useTags;
	}

	public void setImplicitHeaders(boolean implicitHeaders) {
		this.implicitHeaders = implicitHeaders;
	}

	public void setOpenapiDocketConfig(boolean openapiDocketConfig) {
		this.openapiDocketConfig = openapiDocketConfig;
	}

	public void setApiFirst(boolean apiFirst) {
		this.apiFirst = apiFirst;
	}

	public void setHateoas(boolean hateoas) {
		this.hateoasSet = hateoas;
	}

	public void setReturnSuccessCode(boolean returnSuccessCode) {
		this.returnSuccessCode = returnSuccessCode;
	}

	public void setUnhandledException(boolean unhandledException) {
		this.unhandledException = unhandledException;
	}

	@Override
	public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
		super.postProcessModelProperty(model, property);

		if ("null".equals(property.example)) {
			property.example = null;
		}

		// Add imports for Jackson
		if (!Boolean.TRUE.equals(model.isEnum)) {
			model.imports.add("JsonProperty");

			if (Boolean.TRUE.equals(model.hasEnums)) {
				model.imports.add(JSON);
			}
		} else { // enum class
			// Needed imports for Jackson's JsonCreator
			if (additionalProperties.containsKey("jackson")) {
				model.imports.add("JsonCreator");
			}
		}
		model.imports.remove("ApiModel");
		model.imports.remove("ApiModelProperty");
	}

	@Override
	public Map<String, Object> postProcessModelsEnum(Map<String, Object> objs) {
		objs = super.postProcessModelsEnum(objs);

		// Add imports for Jackson
		List<Map<String, String>> imports = (List<Map<String, String>>) objs.get("imports");
		cleanApiModel(imports);
		List<Object> models = (List<Object>) objs.get("models");
		for (Object _mo : models) {
			Map<String, Object> mo = (Map<String, Object>) _mo;
			CodegenModel cm = (CodegenModel) mo.get("model");
			// for enum model
			if (Boolean.TRUE.equals(cm.isEnum) && cm.allowableValues != null) {
				cm.imports.add(importMapping.get(JSON));

				Map<String, String> item = new HashMap<>();
				item.put("import", importMapping.get(JSON));
				imports.add(item);
			}
		}

		return objs;
	}

	private void cleanApiModel(List<Map<String, String>> imports) {
		for (Map<String, String> imp : imports) {
			if (imp.containsValue("io.swagger.annotations.ApiModel")) {
				imports.remove(imp);
				break;
			}
		}
	}

	public void setUseBeanValidation(boolean useBeanValidation) {
		this.useBeanValidation = useBeanValidation;
	}

	public void setPerformBeanValidation(boolean performBeanValidation) {
		this.performBeanValidation = performBeanValidation;
	}

	@Override
	public void setUseOptional(boolean useOptional) {
		this.useOptional = useOptional;
	}

	@Override
	public void postProcessParameter(CodegenParameter p) {
		// we use a custom version of this function to remove the l, d, and f suffixes
		// from Long/Double/Float
		// defaultValues
		// remove the l because our users will use Long.parseLong(String defaultValue)
		// remove the d because our users will use Double.parseDouble(String
		// defaultValue)
		// remove the f because our users will use Float.parseFloat(String defaultValue)
		// NOTE: for CodegenParameters we DO need these suffixes because those
		// defaultValues are used as java value
		// literals assigned to Long/Double/Float
		if (p.defaultValue == null) {
			return;
		}

		if ((p.isLong && "l".equals(p.defaultValue.substring(p.defaultValue.length() - 1)))
				|| (p.isDouble && "d".equals(p.defaultValue.substring(p.defaultValue.length() - 1)))
				|| (p.isFloat && "f".equals(p.defaultValue.substring(p.defaultValue.length() - 1)))) {
			p.defaultValue = p.defaultValue.substring(0, p.defaultValue.length() - 1);
		}
	}

	public void setModule(String name) {

		String camelized = camelize(sanitizeName(name), true);
		camelized = camelized.toLowerCase();

		basePackage = "com.minsait.onesait." + camelized;
		apiPackage = "com.minsait.onesait." + camelized + ".controllers";
		modelPackage = basePackage + ".model";
		invokerPackage = basePackage + ".invoker";
		artifactId = "onesait-" + camelized;
		configPackage = basePackage + ".configuration";
		withXml = false;
		useBeanValidation = true;
		virtualService = false;
		hateoasSet = false;
		dateLibrary = "java8";
		setApiNameSuffix("");
		setModelNameSuffix("DTO");

	}
}