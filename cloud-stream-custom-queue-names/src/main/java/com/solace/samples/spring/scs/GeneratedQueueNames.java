package com.solace.samples.spring.scs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.StaticMessageHeaderAccessor;
import org.springframework.integration.acks.AckUtils;
import org.springframework.integration.acks.AcknowledgmentCallback;
import org.springframework.messaging.Message;

import com.solace.spring.cloud.stream.binder.util.SolaceAcknowledgmentException;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class GeneratedQueueNames {
	private static final Logger log = LoggerFactory.getLogger(GeneratedQueueNames.class);

	public static void main(String[] args) {
		SpringApplication.run(GeneratedQueueNames.class, args);
	}
	
	@Bean
	public Consumer<Message<String>> uppercase() {
		return message -> {		
			log.info("Received message: " + message.getPayload());
		    AcknowledgmentCallback acknowledgmentCallback = StaticMessageHeaderAccessor.getAcknowledgmentCallback(message); // (1)
		    acknowledgmentCallback.noAutoAck(); // (2)
		    try {
		        AckUtils.reject(acknowledgmentCallback); // (3)
		    } catch (SolaceAcknowledgmentException e) {} // (4)
		};
	}
}
