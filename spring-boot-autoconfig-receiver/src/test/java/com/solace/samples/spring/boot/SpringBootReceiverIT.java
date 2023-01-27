package com.solace.samples.spring.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.samples.spring.boot.test.testcontainer.PubSubPlusContainer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.function.ThrowingFunction;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SpringBootReceiver.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@Slf4j
public class SpringBootReceiverIT {
	@Container
	private static final PubSubPlusContainer solacePubSubPlusContainer = new PubSubPlusContainer();

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private MockMvc mockMvc;

	@SpyBean
	private MessageSender messageSender;

	@SpyBean
	private TriggerMessageProcessor triggerMessageProcessor;

	@SpyBean
	private OrderMessageReceiver orderMessageReceiver;

	private static final String TRIGGER_RCV_Q_NAME = RandomStringUtils.randomAlphabetic(100);
	private static final String TRIGGER_SEND_TOPIC_NAME = RandomStringUtils.randomAlphabetic(100);
	private static final String ORDERS_RCV_Q_NAME = RandomStringUtils.randomAlphabetic(100);
	private static final String ORDERS_RCV_POLL_Q_NAME = RandomStringUtils.randomAlphabetic(100);
	private static final String ORDERS_SEND_TOPIC_NAME = RandomStringUtils.randomAlphabetic(100);

	@Test
	public void testWebEndpoint() {
		webTestClient.get().uri("/actuator/health").exchange()
				.expectStatus().isOk()
				.expectBody().json("{\"status\":\"UP\"}");
	}

	@Test
	public void testWebEndpointMock() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/actuator/health"))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().json("{\"status\":\"UP\"}"));
	}

	@Test
	public void testAll() throws Exception {
		webTestClient.get().uri("/order/send").exchange().expectStatus().isCreated();

		log.info("Verifying trigger send");
		Mockito.verify(messageSender).sendTriggerMessage(SpringBootReceiver.Const.TRIGGER_KEY_WORD);

		log.info("Verifying trigger receive (transaction first rejected, then committed on redelivery)");
		ArgumentCaptor<TextMessage> triggerMessageArgumentCaptor = ArgumentCaptor.forClass(TextMessage.class);
		Mockito.verify(triggerMessageProcessor, Mockito.timeout(1000).times(2))
				.processTrigger(triggerMessageArgumentCaptor.capture());
		assertThat(triggerMessageArgumentCaptor.getAllValues())
				.satisfies(l -> {
					assertThat(l.get(0))
							.extracting(ThrowingFunction.of(Message::getJMSRedelivered))
							.isEqualTo(false);
					assertThat(l.get(1))
							.extracting(ThrowingFunction.of(Message::getJMSRedelivered))
							.isEqualTo(true);
				})
				.extracting(ThrowingFunction.of(TextMessage::getText))
				.satisfies(l -> assertThat(new HashSet<>(l)).hasSize(1));

		log.info("Received order message with JMS listener");
		ArgumentCaptor<MyOrder> orderArgumentCaptor = ArgumentCaptor.forClass(MyOrder.class);
		Mockito.verify(orderMessageReceiver, Mockito.timeout(1000)).processOrders(orderArgumentCaptor.capture());
		assertThat(orderArgumentCaptor.getValue()).isNotNull();

		log.info("Received order message from JMSTemplate");
		webTestClient.get().uri("/order/poll").exchange()
				.expectStatus()
				.isOk()
				.expectBody()
				.json(new ObjectMapper().writeValueAsString(orderArgumentCaptor.getValue()));
	}

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
		createQueueWithSubscription(TRIGGER_RCV_Q_NAME, TRIGGER_SEND_TOPIC_NAME);
		createQueueWithSubscription(ORDERS_RCV_POLL_Q_NAME, ORDERS_SEND_TOPIC_NAME);
		createQueueWithSubscription(ORDERS_RCV_Q_NAME, ORDERS_SEND_TOPIC_NAME);
	}

	static void createQueueWithSubscription(String queueName, String subscription) {
		solacePubSubPlusContainer.getSempV2WebClient().post()
				.uri("/config/msgVpns/{msgVpnName}/queues", "default")
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(Map.of(
						"queueName", queueName,
						"egressEnabled", true,
						"ingressEnabled", true,
						"permission", "consume")))
				.retrieve()
				.bodyToMono(String.class)
				.log()
				.block();

		solacePubSubPlusContainer.getSempV2WebClient().post()
				.uri("/config/msgVpns/{msgVpnName}/queues/{queueName}/subscriptions", "default", queueName)
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(Map.of(
						"subscriptionTopic", subscription)))
				.retrieve()
				.bodyToMono(String.class)
				.log()
				.block();
	}
}
