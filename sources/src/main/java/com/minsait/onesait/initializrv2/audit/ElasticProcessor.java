package com.minsait.onesait.initializrv2.audit;


import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.architecture.audit.model.event.AuditEvent;
import com.minsait.onesait.architecture.audit.processor.IProcessor;

import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(value = "architecture.audit.destination.customElastic.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class ElasticProcessor implements IProcessor<AuditEvent> {

	@Value("${architecture.audit.destination.customElastic.elasticServer}")
	private String auditPlatformUrl;
	
	@Value("${architecture.audit.destination.customElastic.userBlackList:[]}")
	private String[] userBlackList;

	@Autowired
	RestTemplate template;

	@Override
	public void init() {
		log.info("Init ElasticService");
	}

	@Override
	public void handle(AuditEvent auditEvent)  {
		ObjectMapper mapper = new ObjectMapper();
		if(Arrays.stream(userBlackList).anyMatch(x -> x.equals(auditEvent.getUser())))
			return;
		
		try {
			if(auditEvent.getOutput()!=null)
				((ObjectNode)auditEvent.getOutput()).remove("body");
			log.info(auditEvent.toString());
			
			
			SimpleModule module = new SimpleModule();
			module.addSerializer(LocalDateTime.class, new DateTimeSerializer());
			mapper.registerModule(module);
			
			String json = mapper.writeValueAsString(auditEvent);
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> requestEntity = new HttpEntity<>(json, responseHeaders);
			template.postForEntity(auditPlatformUrl + "/audit-initializr/_doc", requestEntity, String.class);

		} catch (RestClientException | JsonProcessingException  e) {
			log.error("Error sending data to elasctic"+e.getMessage());
		}
	}

	@Override
	public void close() {
		log.info("Close ElasticService");
	}

}
