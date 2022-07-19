package com.solace.samples.spring.scs;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import com.solace.spring.cloud.stream.binder.messaging.SolaceBinderHeaders;

@SpringBootApplication
public class BatchConsumer {
	private static final Logger log = LoggerFactory.getLogger(BatchConsumer.class);

	public static void main(String[] args) {
		SpringApplication.run(BatchConsumer.class, args);
	}
	
	@Bean
	Consumer<Message<List<byte[]>>> batchConsume() {
		return batchMsg -> { // (1)
			List<?> data = batchMsg.getPayload();
			MessageHeaders headers = batchMsg.getHeaders();
			List<?> dataHeaders = (List<?>) headers.get(SolaceBinderHeaders.BATCHED_HEADERS);

			log.info("Batch Size: " + data.size());
			for (int i=0; i< data.size(); i++) {
				log.info("Batch Headers: " + dataHeaders.get(i));
				log.info("Batch Payload: " + new String((byte[]) data.get(i), StandardCharsets.UTF_8));
			}
		};
	}
}
