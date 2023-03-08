package com.solace.samples.spring.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import java.time.Instant;

/**
 * Demonstrates transactions.
 * When a trigger message is first received, it will fail, causing a transaction rollback.
 * Upon redelivery, the message will successfully be processed, and a new order message will be sent.
 */
@Component
public class TriggerMessageProcessor {
	private static final Logger logger = LoggerFactory.getLogger(TriggerMessageProcessor.class);
	private final MessageSender messageSender;

	public TriggerMessageProcessor(MessageSender messageSender) {
		this.messageSender = messageSender;
	}

	@Transactional
	@JmsListener(destination = "${sample.solace.trigger.receive.queue}")
	public void processTrigger(TextMessage message) throws JMSException {
		if (SpringBootReceiver.Const.TRIGGER_KEY_WORD.equals(message.getText())) {
			logger.info("trigger message received, initiating an internal order");
			if (!message.getJMSRedelivered()) {
				logger.info("trigger message was not redelivered, exception will be thrown");
				throw new RuntimeException("trigger rollback");
			} else {
				logger.info("message was redelivered, accepting successfully");
				messageSender.sendOrderMessage(new MyOrder("internal", 500.0d, Instant.now().toEpochMilli()));
			}
		} else {
			logger.warn("unknown message received:" + message);
		}

	}
}
