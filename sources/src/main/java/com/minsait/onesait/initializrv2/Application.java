package com.minsait.onesait.initializrv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@ComponentScan(basePackages = {"com.minsait.onesait","com.minsait.onesait.architecture"})
public class Application {


	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);

	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
	

}

