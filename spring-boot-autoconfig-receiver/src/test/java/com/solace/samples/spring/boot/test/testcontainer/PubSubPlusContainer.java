package com.solace.samples.spring.boot.test.testcontainer;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

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
}
