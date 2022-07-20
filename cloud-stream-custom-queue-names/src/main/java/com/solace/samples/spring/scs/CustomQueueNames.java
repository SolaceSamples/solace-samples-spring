package com.solace.samples.spring.scs;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

@SpringBootApplication
public class CustomQueueNames {
	private static final Logger log = LoggerFactory.getLogger(CustomQueueNames.class);

	public static void main(String[] args) {
		SpringApplication.run(CustomQueueNames.class, args);
	}
	
	@Bean
	public Consumer<Message<String>> uppercase() {
		return message -> {		
			log.info("Received message: " + message.getPayload());
		};
	}
	
	@Bean
	public Consumer<Message<String>> lowercase() {
		return message -> {		
			log.info("Received message: " + message.getPayload());
		};
	}
}
