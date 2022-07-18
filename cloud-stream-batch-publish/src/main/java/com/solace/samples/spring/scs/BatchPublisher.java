package com.solace.samples.spring.scs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

@SpringBootApplication
public class BatchPublisher {
	public static void main(String[] args) {
		SpringApplication.run(BatchPublisher.class, args);
	}
	
	@Bean
	public Function<String, Collection<Message<String>>> batchPublish() {
	    return v -> {
	        System.out.println("Received trigger to publish Batch of Messages");
	        
	        ArrayList<Message<String>> msgList = new ArrayList<Message<String>>();
	        msgList.add(MessageBuilder.withPayload("Payload 1").build());
	        msgList.add(MessageBuilder.withPayload("Payload 2").build());
	        msgList.add(MessageBuilder.withPayload("Payload 3").build());
	        msgList.add(MessageBuilder.withPayload("Payload 4").build());
	        msgList.add(MessageBuilder.withPayload("Payload 5").build());
	        
	        System.out.println("Publish Batch of 5 Messages");
	        return msgList;
	    };
	}
}
