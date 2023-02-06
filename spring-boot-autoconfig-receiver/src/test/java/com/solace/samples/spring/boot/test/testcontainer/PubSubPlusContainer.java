package com.solace.samples.spring.boot.test.testcontainer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Slf4j
public class PubSubPlusContainer extends GenericContainer<PubSubPlusContainer> {
	private static final String ADMIN_USERNAME = "admin";
	private static final String ADMIN_PASSWORD = "admin";

	public PubSubPlusContainer() {
		super("solace/solace-pubsub-standard:10.2");
		withExposedPorts(8080, 55555)
				.withEnv("username_admin_globalaccesslevel", ADMIN_USERNAME)
				.withEnv("username_admin_password", ADMIN_PASSWORD)
				.withEnv("system_scaling_maxconnectioncount", "100")
				.withSharedMemorySize((long) Math.pow(1024, 3)) // 1 GB
				.waitingFor(Wait.forHttp("/SEMP/v2/monitor/about")
						.forPort(8080)
						.withBasicCredentials(ADMIN_USERNAME, ADMIN_PASSWORD));
	}

	public void registerCredentials(DynamicPropertyRegistry dynamicPropertyRegistry) {
		dynamicPropertyRegistry.add("solace.jms.host",
				() -> String.format("smf://%s:%s", getHost(), getMappedPort(55555)));
		dynamicPropertyRegistry.add("solace.jms.msgVpn", () -> "default");
		dynamicPropertyRegistry.add("solace.jms.clientUsername", () -> "default");
		dynamicPropertyRegistry.add("solace.jms.clientPassword", () -> "default");
	}

	public WebClient getSempV2WebClient() {
		return WebClient.builder()
				.baseUrl(String.format("http://%s:%s/SEMP/v2", getHost(), getMappedPort(8080)))
				.defaultHeaders(headers -> headers.setBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD))
				.build();
	}

	public void createQueueWithSubscription(String queueName, String subscription) {
		log.info("Creating queue {}", queueName);
		getSempV2WebClient().post()
				.uri("/config/msgVpns/{msgVpnName}/queues", "default")
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(Map.of(
						"queueName", queueName,
						"egressEnabled", true,
						"ingressEnabled", true,
						"permission", "consume")))
				.retrieve()
				.bodyToMono(String.class)
				.retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
				.doOnError(WebClientResponseException.class,
						e -> log.error("failed to create queue: {}", e.getResponseBodyAsString(), e))
				.log()
				.block();

		log.info("Adding subscription {} to queue {}", subscription, queueName);
		getSempV2WebClient().post()
				.uri("/config/msgVpns/{msgVpnName}/queues/{queueName}/subscriptions", "default", queueName)
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(Map.of(
						"subscriptionTopic", subscription)))
				.retrieve()
				.bodyToMono(String.class)
				.retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
				.doOnError(WebClientResponseException.class,
						e -> log.error("failed to create queue: {}", e.getResponseBodyAsString(), e))
				.log()
				.block();
	}

	public void createJndiObject(String objectType, String name, String physicalName) {
		String jndiPathParam;
		String refField;

		switch (objectType) {
			case "queue" -> {
				jndiPathParam = "jndiQueues";
				refField = "queueName";
			}
			case "topic" -> {
				jndiPathParam = "jndiTopics";
				refField = "topicName";
			}
			default -> throw new IllegalArgumentException(objectType);
		}

		log.info("Creating JNDI {} {} for physical {} {}", objectType, name, objectType, physicalName);
		getSempV2WebClient().post()
				.uri("/config/msgVpns/{msgVpnName}/{jndiObjectType}", "default", jndiPathParam)
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(Map.of(
						"physicalName", physicalName,
						refField, name
				)))
				.retrieve()
				.bodyToMono(String.class)
				.retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
				.doOnError(WebClientResponseException.class,
						e -> log.error("failed to create queue: {}", e.getResponseBodyAsString(), e))
				.log()
				.block();
	}
}
