package com.solace.samples.spring.scs;

import java.util.function.Function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.StaticMessageHeaderAccessor;
import org.springframework.integration.acks.AckUtils;
import org.springframework.integration.acks.AcknowledgmentCallback;
import org.springframework.messaging.Message;

import com.solace.spring.cloud.stream.binder.messaging.SolaceHeaders;
import com.solace.spring.cloud.stream.binder.util.SolaceAcknowledgmentException;

@SpringBootApplication
public class ManualAcknowledgement {
	public static void main(String[] args) {
		SpringApplication.run(ManualAcknowledgement.class, args);
	}
	
	@Bean
	public Function<Message<String>, String> manualAckFunction() {
        return message -> {
        	System.out.println("Received message on TOPIC: " + message.getHeaders().get(SolaceHeaders.DESTINATION));

            // Disable Auto-Acknowledgement
            AcknowledgmentCallback ackCallback = StaticMessageHeaderAccessor.getAcknowledgmentCallback(message);
            ackCallback.noAutoAck();

            // Use CorrelationID for easy business logic...
            String cid = (String) message.getHeaders().get(SolaceHeaders.CORRELATION_ID);
            if (cid == null)
				cid = "accept";
            
        	System.out.println("Message Correlation Id: " + cid);
            // Invoke appropriate Acknowledge action based on the correlation-id
            try {
                if (cid.equals("accept")) {
                    System.out.println("Accepting the Message");
                    AckUtils.accept(ackCallback);
                    return "Accepted the Message";
                } else if (cid.equals("requeue")) {
                	System.out.println("Requeuing the Message");
                    AckUtils.requeue(ackCallback);
                    return "Requeuing the Message";
                } else {
                	// if not accept or requeue, default to reject acknowledgement mode
                	System.out.println("Rejecting the Message");
                    AckUtils.reject(ackCallback);
                    Thread.sleep(10000);
                    return "Rejecting the Message";
                }
            } catch (SolaceAcknowledgmentException e) {
            	System.out.println("Warning, exception occurred but message will be re-queued on broker and re-delivered" + e);
                return null; //Don't send an output message
            } catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}

        };
	}
}
