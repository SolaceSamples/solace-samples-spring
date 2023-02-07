package com.solace.samples.spring.boot;

import com.solace.samples.spring.boot.test.testcontainer.PubSubPlusContainer;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class SpringBootReceiverJndiIT  extends SpringBootReceiverITBase {
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
		dynamicPropertyRegistry.add("sample.solace.jndi-name", () -> "/jms/cf/default");
		dynamicPropertyRegistry.add("sample.solace.trigger.receive.queue", () -> TRIGGER_RCV_Q_NAME);
		dynamicPropertyRegistry.add("sample.solace.trigger.send.topic", () -> TRIGGER_SEND_TOPIC_NAME);
		dynamicPropertyRegistry.add("sample.solace.orders.poll.receive.queue", () -> ORDERS_RCV_POLL_Q_NAME);
		dynamicPropertyRegistry.add("sample.solace.orders.receive.queue", () -> ORDERS_RCV_Q_NAME);
		dynamicPropertyRegistry.add("sample.solace.orders.send.topic", () -> ORDERS_SEND_TOPIC_NAME);
	}

	@BeforeAll
	static void beforeAll() {
		String triggerRcvQPhysicalName = RandomStringUtils.randomAlphabetic(100);
		solacePubSubPlusContainer.createJndiObject("queue", TRIGGER_RCV_Q_NAME, triggerRcvQPhysicalName);

		String ordersRcvQPhysicalName = RandomStringUtils.randomAlphabetic(100);
		solacePubSubPlusContainer.createJndiObject("queue", ORDERS_RCV_Q_NAME, ordersRcvQPhysicalName);

		String ordersRcvPollQPhysicalName = RandomStringUtils.randomAlphabetic(100);
		solacePubSubPlusContainer.createJndiObject("queue", ORDERS_RCV_POLL_Q_NAME, ordersRcvPollQPhysicalName);

		String triggerSendTopicPhysicalName = RandomStringUtils.randomAlphabetic(100);
		solacePubSubPlusContainer.createJndiObject("topic", TRIGGER_SEND_TOPIC_NAME, triggerSendTopicPhysicalName);

		String ordersSendTopicPhysicalName = RandomStringUtils.randomAlphabetic(100);
		solacePubSubPlusContainer.createJndiObject("topic", ORDERS_SEND_TOPIC_NAME, ordersSendTopicPhysicalName);

		solacePubSubPlusContainer.createQueueWithSubscription(triggerRcvQPhysicalName, triggerSendTopicPhysicalName);
		solacePubSubPlusContainer.createQueueWithSubscription(ordersRcvQPhysicalName, ordersSendTopicPhysicalName);
		solacePubSubPlusContainer.createQueueWithSubscription(ordersRcvPollQPhysicalName, ordersSendTopicPhysicalName);
	}
}