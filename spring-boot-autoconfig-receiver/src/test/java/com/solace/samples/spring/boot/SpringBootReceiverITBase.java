package com.solace.samples.spring.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.function.ThrowingFunction;

import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SpringBootReceiver.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@Slf4j
public abstract class SpringBootReceiverITBase {
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

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
				.json(OBJECT_MAPPER.writeValueAsString(orderArgumentCaptor.getValue()));
	}
}
