package com.solace.samples.spring.boot;

import com.solace.samples.spring.boot.test.testcontainer.PubSubPlusContainer;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.function.ThrowingFunction;
import org.springframework.util.function.ThrowingSupplier;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SpringBootReceiver.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Disabled("incomplete")
public class SpringBootReceiverJndiIT {
	@Container
	private static final PubSubPlusContainer solacePubSubPlusContainer = new PubSubPlusContainer();

	private static final String RECEIVE_JNDI_QUEUE_NAME = RandomStringUtils.randomAlphabetic(100);
	private static final String RECEIVE_PHYSICAL_QUEUE_NAME = RandomStringUtils.randomAlphabetic(100);

	@Autowired
	private JmsTemplate jmsTemplate;

//	@SpyBean
//	private SpringBootReceiver.MessageListener messageListener;

	@Test
	public void testSend() {
		String physicalQueueName = RandomStringUtils.randomAlphabetic(100);
		String jndiQueueName = RandomStringUtils.randomAlphabetic(100);
		String payload = "hello world";

		TemporaryQueue queue = jmsTemplate.execute(Session::createTemporaryQueue);
		solacePubSubPlusContainer.getSempV2WebClient()
				.post()
				.uri("/msgVpns/default/jndiQueues")
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(Map.of(
						"msgVpnName", "default",
						"physicalName", ThrowingSupplier.of(queue::getQueueName),
						"queueName", jndiQueueName)))
				.retrieve()
				.bodyToMono(String.class);

		jmsTemplate.convertAndSend(jndiQueueName, payload);
		assertThat(jmsTemplate.receive(jndiQueueName))
				.asInstanceOf(InstanceOfAssertFactories.type(TextMessage.class))
				.extracting(ThrowingFunction.of(TextMessage::getText))
				.isEqualTo(payload);
	}

	@Test
	public void testReceive() {
		String payload = "hello world";
		jmsTemplate.send(RECEIVE_PHYSICAL_QUEUE_NAME, messageCreator -> messageCreator.createTextMessage(payload));
		ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
//		Mockito.verify(messageListener, Mockito.timeout(1000)).handle(messageArgumentCaptor.capture());
		assertThat(messageArgumentCaptor.getValue())
				.asInstanceOf(InstanceOfAssertFactories.type(TextMessage.class))
				.extracting(ThrowingFunction.of(TextMessage::getText))
				.isEqualTo(payload);
	}

	@DynamicPropertySource
	static void configureTestSpringProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
		solacePubSubPlusContainer.registerCredentials(dynamicPropertyRegistry);
		dynamicPropertyRegistry.add("solace.jms.apiProperties.Solace_JMS_DynamicDurables", () -> "true");
		dynamicPropertyRegistry.add("spring.jms.jndi-name", () -> "/jms/cf/default");
		dynamicPropertyRegistry.add("sample.solace.receive.queue", () -> RECEIVE_JNDI_QUEUE_NAME);
	}
}
