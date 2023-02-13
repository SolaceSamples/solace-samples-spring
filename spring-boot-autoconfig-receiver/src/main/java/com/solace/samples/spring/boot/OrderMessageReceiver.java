package com.solace.samples.spring.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class OrderMessageReceiver {
	private static final Logger logger = LoggerFactory.getLogger(OrderMessageReceiver.class);

	@JmsListener(destination = "${sample.solace.orders.receive.queue}")
	public void processOrders(MyOrder message) {
		logger.info("new order message received: {}", message.toString());
	}
}
