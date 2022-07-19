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
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TemperatureSinkTest {

	@Autowired
	private ApplicationContext context;

	@SuppressWarnings("unused")
	@Test
	public void testSink() {
		BeanFactoryChannelResolver channelResolver = context.getBean("integrationChannelResolver",
				BeanFactoryChannelResolver.class);
		/*
		 * Spring doesn't allow sending messages with NULL payload. Hence, a unit test can not be 
		 * successfully built or run. 
		 * 
		 * However to test this feature, we can follow any of the following options manually:
		 * 
		 * 1. Use of Try Me! tool in the PubSub+ Manager tool
		 * 		Publish events on the topic with empty message content
		 * 2. Use sdkperf to publish events on the topic with emptry payload
		 * 		./sdkperf_java.sh -cip="localhost:55554" -ptl "sensor/temperature/99" -mn 1 -mr 1 -md
		 */
	}

}
