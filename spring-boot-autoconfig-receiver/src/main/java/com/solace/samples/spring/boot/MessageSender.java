package com.solace.samples.spring.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import jakarta.jms.Destination;
import java.util.Objects;

@Component
@EnableJms
public class MessageSender {

	private final JmsTemplate jmsTemplate;

	@Value("${sample.solace.trigger.send.topic}")
	private String triggerSendTopic;

	@Value("${sample.solace.orders.send.topic}")
	private String orderSendTopic;


	private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

	public MessageSender(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public void sendTriggerMessage(String message) {
		logger.info("Sending trigger message {} to {}", message, triggerSendTopic);
		// Forcing destination resolution to use topics
		Destination destination = jmsTemplate.execute(s -> jmsTemplate.getDestinationResolver()
				.resolveDestinationName(s, triggerSendTopic, true));
		jmsTemplate.send(Objects.requireNonNull(destination), s -> s.createTextMessage(message));
	}

	public void sendOrderMessage(MyOrder message) {
		logger.info("Sending order message {} to {}", message, orderSendTopic);
		// Forcing destination resolution to use topics
		Destination destination = jmsTemplate.execute(s -> jmsTemplate.getDestinationResolver()
				.resolveDestinationName(s, orderSendTopic, true));
		jmsTemplate.convertAndSend(Objects.requireNonNull(destination), message);
	}
}
