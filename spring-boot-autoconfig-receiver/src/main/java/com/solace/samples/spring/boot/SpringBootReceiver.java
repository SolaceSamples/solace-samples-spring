package com.solace.samples.spring.boot;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.JmsListener;

@SpringBootApplication
public class SpringBootReceiver {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReceiver.class, args);
	}

	@JmsListener(destination = "SpringTestQueue")
	public void handle(Message message) {

		Date receiveTime = new Date();

		if (message instanceof TextMessage) {
			TextMessage tm = (TextMessage) message;
			try {
				System.out.println(
						"Message Received at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(receiveTime)
								+ " with message content of: " + tm.getText());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println(message.toString());
		}
	}

}
