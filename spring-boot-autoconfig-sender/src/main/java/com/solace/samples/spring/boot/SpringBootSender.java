package com.solace.samples.spring.boot;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class SpringBootSender {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootSender.class, args);
	}

	@Autowired
	private JmsTemplate jmsTemplate;

	@PostConstruct
	private void customizeJmsTemplate() {
		// Update the jmsTemplate's connection factory to cache the connection
		CachingConnectionFactory ccf = new CachingConnectionFactory();
		ccf.setTargetConnectionFactory(jmsTemplate.getConnectionFactory());
		jmsTemplate.setConnectionFactory(ccf);

		// By default Spring Integration uses Queues, but if you set this to true you
		// will send to a PubSub+ topic destination
		jmsTemplate.setPubSubDomain(false);
	}

	@Value("SpringTestQueue")
	private String queueName;

	@Scheduled(fixedRate = 5000)
	public void sendEvent() throws Exception {
		String msg = "Hello World " + System.currentTimeMillis();
		System.out.println("==========SENDING MESSAGE========== " + msg);
		jmsTemplate.convertAndSend(queueName, msg);
	}

}
