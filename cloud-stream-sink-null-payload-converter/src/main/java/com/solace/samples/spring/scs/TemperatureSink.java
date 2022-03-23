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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.StreamMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.converter.MessageConverter;

import com.solace.samples.spring.common.SensorReading;

@SpringBootApplication
public class TemperatureSink {

	public static void main(String[] args) {
		SpringApplication.run(TemperatureSink.class, args);
	}

	/*
	 *  Check out application.yml to see how to
	 *  1. Use `concurrency` for multi-threaded consumption
	 *  2. Use wildcard subscriptions
	 */
	@Bean
	public Consumer<SensorReading> sink(){
		return System.out::println;
	}
	
	/*
	 * Spring Cloud Stream exposes a mechanism to define and register additional MessageConverters. 
	 * To use it, implement org.springframework.messaging.converter.MessageConverter, and configure it as a @Bean 
	 * It is then appended to the existing stack of `MessageConverter`s.
	 */
    @Bean
    public MessageConverter customMessageConverter() {
        return new NullSensorReadingConverter();
    }

}
