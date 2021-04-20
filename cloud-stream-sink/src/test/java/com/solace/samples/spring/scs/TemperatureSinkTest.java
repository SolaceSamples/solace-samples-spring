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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.solace.samples.spring.common.SensorReading;
import com.solace.samples.spring.common.SensorReading.BaseUnit;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TemperatureSinkTest {

	@Autowired
	private ApplicationContext context;

	@Test
	public void testSink() {
		BeanFactoryChannelResolver channelResolver = context.getBean("integrationChannelResolver",
				BeanFactoryChannelResolver.class);
		MessageChannel channel = channelResolver.resolveDestination("sink-in-0");
		channel.send(MessageBuilder.withPayload(new SensorReading("test", 50, BaseUnit.FAHRENHEIT)).build());
	}

}
