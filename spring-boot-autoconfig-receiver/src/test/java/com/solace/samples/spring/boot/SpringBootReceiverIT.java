package com.solace.samples.spring.boot;

import com.solace.samples.spring.boot.test.testcontainer.PubSubPlusContainer;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class SpringBootReceiverIT extends SpringBootReceiverITBase {
	@Container
	private static final PubSubPlusContainer solacePubSubPlusContainer = new PubSubPlusContainer();

	private static final String TRIGGER_RCV_Q_NAME = RandomStringUtils.randomAlphabetic(100);
	private static final String TRIGGER_SEND_TOPIC_NAME = RandomStringUtils.randomAlphabetic(100);
	private static final String ORDERS_RCV_Q_NAME = RandomStringUtils.randomAlphabetic(100);
	private static final String ORDERS_RCV_POLL_Q_NAME = RandomStringUtils.randomAlphabetic(100);
	private static final String ORDERS_SEND_TOPIC_NAME = RandomStringUtils.randomAlphabetic(100);

	@DynamicPropertySource
	static void configureTestSpringProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
		solacePubSubPlusContainer.registerCredentials(dynamicPropertyRegistry);
		dynamicPropertyRegistry.add("sample.solace.trigger.receive.queue", () -> TRIGGER_RCV_Q_NAME);
		dynamicPropertyRegistry.add("sample.solace.trigger.send.topic", () -> TRIGGER_SEND_TOPIC_NAME);
		dynamicPropertyRegistry.add("sample.solace.orders.poll.receive.queue", () -> ORDERS_RCV_POLL_Q_NAME);
		dynamicPropertyRegistry.add("sample.solace.orders.receive.queue", () -> ORDERS_RCV_Q_NAME);
		dynamicPropertyRegistry.add("sample.solace.orders.send.topic", () -> ORDERS_SEND_TOPIC_NAME);
	}

	@BeforeAll
	static void beforeAll() {
		solacePubSubPlusContainer.createQueueWithSubscription(TRIGGER_RCV_Q_NAME, TRIGGER_SEND_TOPIC_NAME);
		solacePubSubPlusContainer.createQueueWithSubscription(ORDERS_RCV_POLL_Q_NAME, ORDERS_SEND_TOPIC_NAME);
		solacePubSubPlusContainer.createQueueWithSubscription(ORDERS_RCV_Q_NAME, ORDERS_SEND_TOPIC_NAME);
	}
}
