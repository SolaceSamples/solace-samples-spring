package com.solace.samples.spring.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Objects;

@RestController
@RequestMapping("/order")
public class RestControllerForOrders implements ErrorController {
	private static final Logger log = LoggerFactory.getLogger(RestControllerForOrders.class);

	private final MessageSender messageSender;
	private final JmsTemplate jmsTemplate;

	@Value("${sample.solace.orders.poll.receive.queue}")
	private String pollQueue;

	public RestControllerForOrders(MessageSender messageSender, JmsTemplate jmsTemplate) {
		this.messageSender = messageSender;
		this.jmsTemplate = jmsTemplate;
	}


	@GetMapping("/send")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public String sendOrder() {
		log.info("Sending a new trigger for the order");
		try {
			messageSender.sendTriggerMessage(SpringBootReceiver.Const.TRIGGER_KEY_WORD);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Actor Not Found", e);
		}
		return "Thank you for sending an order";
	}

	@GetMapping("/poll")
	@ResponseBody
	public String pollOrder() {
		log.info("Polling order");
		Message message = jmsTemplate.receive(pollQueue);
		log.info("Polled message: {}", message);
		try {
			return ((TextMessage) Objects.requireNonNull(message)).getText();
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

}
