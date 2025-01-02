/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.solace.samples.spring.scs;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootApplication
public class DynamicDestinationProcessor {

	private static final Logger log = LoggerFactory.getLogger(DynamicDestinationProcessor.class);
	private static AtomicInteger counter = new AtomicInteger(0);

	public static void main(String[] args) {
		SpringApplication.run(DynamicDestinationProcessor.class);
	}

	/*
	 * Dynamic Topic Publish OPTION 1: Using scst_targetDestination header; works
	 * with any binder that supports the header, including Solace.
	 * 
	 * Note that the BinderHeaders.TARGET_DESTINATION header is essentially telling the
	 * binder to override the default destination specified on a binding and if the
	 * header is NOT set then the message would be sent to the default destination.
	 */
	@Bean
	public Function<Message<String>, Message<String>> functionUsingTargetDestHeader() {
		return input -> {
			String topic = getMyTopicUsingLogic(input.getPayload());
			log.info("Processing message: " + input.getPayload());
			String payload = input.getPayload().concat(" Processed by functionUsingTargetDestHeader");
      log.info("Setting dynamic target destination to (functionUsingTargetDestHeader): " + topic);

			return MessageBuilder.withPayload(payload).setHeader(BinderHeaders.TARGET_DESTINATION, topic).build();
		};
	}

	/*
	 * Dynamic Topic Publish OPTION 2: Using StreamBridge; works with any binder
	 * 
	 * StreamBridge caches a channel within Spring for each destination.
	 * 
	 * The number of channels cached is configurable via
	 * `spring.cloud.stream.dynamic-destination-cache-size`
	 */
	@Bean
	public Consumer<String> functionUsingStreamBridge(StreamBridge streamBridge) {
		return input -> {
			String topic = getMyTopicUsingLogic(input);
			log.info("Processing message (functionUsingStreamBridge): " + input);
			String payload = input.concat(" Processed by functionUsingStreamBridge");
      log.info("Setting dynamic target destination to (functionUsingStreamBridge): " + topic);
			streamBridge.send(topic, payload);
		};
	}

	private String getMyTopicUsingLogic(String input) {
		// TODO Use whatever logic you'd like!
		return "pub/sub/plus/".concat(String.valueOf(counter.incrementAndGet()));
	}

	// FUNCTIONS BELOW ARE PURELY FOR TESTING
	@Bean
	public Supplier<String> supplierTargetDestination() {
		return () -> "hello world targetDestination";
	}

	@Bean
	public Supplier<String> supplierStreamBridge() {
		return () -> "hello world stream bridge";
	}

	@Bean
	public Consumer<String> receiveAll() {
		return (s) -> log.info("receiveAll received "+s);
	}
}
