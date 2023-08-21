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

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ErrorMessage;

@SpringBootApplication
public class ConsumerErrorHandlerDemo {

	private static final Logger log = LoggerFactory.getLogger(ConsumerErrorHandlerDemo.class);
	public static void main(String[] args) {
		SpringApplication.run(ConsumerErrorHandlerDemo.class, args);
	}

	@Bean
	public Consumer<Message<String>> functionOne() {
		return message -> {		
			log.info("Received message on binding <functionOne-in-0>: " + message.getPayload());

			// throw exception
			throw new RuntimeException("Exception thrown");
		};
	}

	@Bean
	public Consumer<Message<String>> functionTwo() {
		return message -> {		
			log.info("Received message on binding <functionTwo-in-0>: " + message.getPayload());
			// throw exception
			throw new RuntimeException("Exception thrown");
		};
	}

	@Bean
	public Consumer<ErrorMessage> binderSpecificErrorHandler() {
		return message -> {
			// ErrorMessage received on a binder
			log.info("Received error message on binder-specific error handler");
			log.info("Original Message: " + message.getOriginalMessage());
		};
	}


	@Bean
	public Consumer<ErrorMessage> defaultErrorHandler() {
		return message -> {
			// ErrorMessage received on default error handler
			log.info("Received error message on default error handler");
			log.info("Original Message: " + message.getPayload());
		};
	}
}
