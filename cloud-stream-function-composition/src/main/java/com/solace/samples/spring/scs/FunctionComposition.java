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

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import com.fasterxml.jackson.databind.JsonNode;

@SpringBootApplication
public class FunctionComposition {

	private static final Logger log = LoggerFactory.getLogger(FunctionComposition.class);

	public static void main(String[] args) {
		SpringApplication.run(FunctionComposition.class);
	}

	/*
	 * This example shows function composition; or basically chaining of functions
	 * in Spring Cloud Stream.
	 * 
	 * Note a few things:
	 * 
	 * 1. These functions are independent of each other and can be re-used.
	 * 
	 * 2. Bindings & composition are setup in application.yml
	 * 
	 * 3. Spring will attempt to do Type Conversion between functions (Note how
	 * preprocess outputs a String and process takes in a JsonNode)
	 * 
	 * 4. Although not shown here you can combine imperative and reactive functions
	 */
	@Bean
	public Function<Message<Object>, String> preProcess() {
		return input -> {
			/*
			 * Apply your business logic, maybe something like:
			 * 
			 * 1. Common Validation
			 * 
			 * 2. Convert from external to internal data model
			 *
			 */
			log.info("preProcess: " + input);
			return "{\"preProcess\":\"says hello\"}";
		};
	}

	@Bean
	public Function<JsonNode, Object> process() {
		return input -> {
			/*
			 * Apply your business logic
			 */
			log.info("process: " + input);
			return "Hello World from process";
		};
	}

	@Bean
	public Function<Object, String> postProcess() {
		return input -> {
			/*
			 * Apply your post processing business logic, maybe something like:
			 * 
			 * 1. Validate internal objects are valid
			 * 
			 * 2. Convert from internal to external data model
			 */
			log.info("postProcess: " + input);
			return "postProcess is complete!";
		};
	}
}
