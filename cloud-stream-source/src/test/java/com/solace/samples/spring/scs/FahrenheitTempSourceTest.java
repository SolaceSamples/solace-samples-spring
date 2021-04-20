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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FahrenheitTempSourceTest {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private MessageCollector collector;

	@Test
	public void testEmitSensorReading() throws Exception {
		BeanFactoryChannelResolver channelResolver = context.getBean("integrationChannelResolver",
				BeanFactoryChannelResolver.class);
		MessageChannel channel = channelResolver.resolveDestination("emitSensorReading-out-0");
		Message<?> msg = (Message<?>) collector.forChannel(channel).poll(1, TimeUnit.SECONDS);
		Object payload = (msg != null) ? msg.getPayload() : null;

		assertThat((String) payload, allOf(containsString("sensorID"), containsString("temperature"),
				containsString("baseUnit"), containsString("timestamp"), containsString("FAHRENHEIT")));
	}
}