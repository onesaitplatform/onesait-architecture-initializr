package com.minsait.onesait.initializrv2.configuration;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path}")
	String contextPath;
    
    @Value("${springdoc.swagger-ui.oauth.tokenUrl}")
	String tokenUrl;
     
    
  
    private static final String BEARER = "TOKEN";

    @Bean
	public OpenAPI customOpenAPI( BuildProperties buildProperties) {
		// define the apiKey SecuritySchema
		return new OpenAPI()
				.components(new Components().addSecuritySchemes(BEARER,
						apiKeySecuritySchema()))
				.info(new Info().version(buildProperties.getVersion()).title("onesait-initializrv2 API")
						.description("RESTful services documentation with OpenAPI 3."))
				.security(Arrays.asList(new SecurityRequirement().addList(BEARER)))
				.addServersItem(new Server().url(contextPath));
	}
	public SecurityScheme apiKeySecuritySchema() {
		return new SecurityScheme().name("Authorization").description("Description about the TOKEN").type(Type.HTTP)
				.bearerFormat("JWT").scheme("bearer");
	}
	
   
}
