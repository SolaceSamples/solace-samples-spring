# cloud-stream-sink - Using Spring Cloud Stream Consumer

The `TemperatureSink` application is a Spring Boot application that leverages Spring Cloud Stream to consume sensor readings (objects of type `SensorReading`) from a message broker.

## Requirements

To run this sample, you will need to have installed:

- Java 17 or Above

## Code Tour

In the `TemperatureSink` application, review the source code which consumes sensor readings published on the broker and simply prints the message content on the console.

```java
@Bean
public Consumer<SensorReading> sink(){
  return System.out::println;
}
```

## Running the application

Make sure to update the Solace Broker connection details with the appropriate host, msgVpn, client username, and password in `application.yml`.

```sh
cd cloud-stream-sink
mvn clean spring-boot:run
```

This will start the Spring Boot application and create a queue with subscription to topic ```sensor/temperature/>``` and waits for messages to arrive.

```
2025-01-02T14:19:23.656+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.i.JCSMPInboundChannelAdapter : Creating consumer 1 of 5 for inbound adapter dbca4f5b-b176-4c67-a052-5d92ff2492d0
2025-01-02T14:19:23.692+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.i.JCSMPInboundChannelAdapter : Creating consumer 2 of 5 for inbound adapter dbca4f5b-b176-4c67-a052-5d92ff2492d0
2025-01-02T14:19:23.693+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.i.JCSMPInboundChannelAdapter : Creating consumer 3 of 5 for inbound adapter dbca4f5b-b176-4c67-a052-5d92ff2492d0
2025-01-02T14:19:23.694+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.i.JCSMPInboundChannelAdapter : Creating consumer 4 of 5 for inbound adapter dbca4f5b-b176-4c67-a052-5d92ff2492d0
2025-01-02T14:19:23.694+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.i.JCSMPInboundChannelAdapter : Creating consumer 5 of 5 for inbound adapter dbca4f5b-b176-4c67-a052-5d92ff2492d0
2025-01-02T14:19:23.696+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.util.FlowReceiverContainer   : Binding flow receiver container 5d027806-8840-42c7-afb2-ce0c6d086e96
2025-01-02T14:19:23.696+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.util.FlowReceiverContainer   : Flow receiver container 5d027806-8840-42c7-afb2-ce0c6d086e96 started in state 'Running'
2025-01-02T14:19:23.703+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.util.FlowReceiverContainer   : Binding flow receiver container 0bc3a187-83f9-4208-8186-9d27b66cd46d
2025-01-02T14:19:23.703+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.util.FlowReceiverContainer   : Flow receiver container 0bc3a187-83f9-4208-8186-9d27b66cd46d started in state 'Running'
2025-01-02T14:19:23.706+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.util.FlowReceiverContainer   : Binding flow receiver container 136f5825-992a-4b8b-8085-383910d9d609
2025-01-02T14:19:23.706+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.util.FlowReceiverContainer   : Flow receiver container 136f5825-992a-4b8b-8085-383910d9d609 started in state 'Running'
2025-01-02T14:19:23.709+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.util.FlowReceiverContainer   : Binding flow receiver container 98da8fd5-b438-479f-8962-f30563541f99
2025-01-02T14:19:23.709+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.util.FlowReceiverContainer   : Flow receiver container 98da8fd5-b438-479f-8962-f30563541f99 started in state 'Running'
2025-01-02T14:19:23.712+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.util.FlowReceiverContainer   : Binding flow receiver container 825cd09e-7dd3-4b72-b896-634e56a38b90
2025-01-02T14:19:23.712+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.util.FlowReceiverContainer   : Flow receiver container 825cd09e-7dd3-4b72-b896-634e56a38b90 started in state 'Running'
2025-01-02T14:19:23.720+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.p.SolaceQueueProvisioner     : Subscribing queue scst/wk/SINK/plain/TEMPS.Q to topic TEMPS.Q
2025-01-02T14:19:23.724+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.p.SolaceQueueProvisioner     : Subscribing queue scst/wk/SINK/plain/TEMPS.Q to topic sensor/temperature/>
2025-01-02T14:19:23.726+05:30  INFO 16436 --- [  restartedMain] c.s.s.c.s.b.i.JCSMPInboundChannelAdapter : started com.solace.spring.cloud.stream.binder.inbound.JCSMPInboundChannelAdapter@deb5bccf
2025-01-02T14:19:23.745+05:30  INFO 16436 --- [  restartedMain] c.s.samples.spring.scs.TemperatureSink   : Started TemperatureSink in 3.961 seconds (process running for 4.542)
```

In another the terminal, start the ```cloud-stream-source``` application.

```sh
cd cloud-stream-source
mvn clean spring-boot:run
```

This will start the Spring Boot application publish messages on topic ```sensor/temperature/fahrenheit```.


```
2025-01-02T14:22:02.241+05:30  INFO 16675 --- [   scheduling-1] c.s.s.spring.scs.FahrenheitTempSource    : Emitting SensorReading [ 2025-01-02 14:22:02.241 1ea53e10-cf0d-428a-839a-eb98fa4eb8a4 6.8 FAHRENHEIT ]
2025-01-02T14:22:07.245+05:30  INFO 16675 --- [   scheduling-1] c.s.s.spring.scs.FahrenheitTempSource    : Emitting SensorReading [ 2025-01-02 14:22:07.245 1ea53e10-cf0d-428a-839a-eb98fa4eb8a4 28.6 FAHRENHEIT ]
2025-01-02T14:22:12.248+05:30  INFO 16675 --- [   scheduling-1] c.s.s.spring.scs.FahrenheitTempSource    : Emitting SensorReading [ 2025-01-02 14:22:12.248 1ea53e10-cf0d-428a-839a-eb98fa4eb8a4 15.3 FAHRENHEIT ]
2025-01-02T14:22:17.252+05:30  INFO 16675 --- [   scheduling-1] c.s.s.spring.scs.FahrenheitTempSource    : Emitting SensorReading [ 2025-01-02 14:22:17.251 1ea53e10-cf0d-428a-839a-eb98fa4eb8a4 38.6 FAHRENHEIT ]
```

On the terminal where the cloud-stream-sink application is running, you can see the arrival of the published messages.

```
SensorReading [ 2025-01-02 14:22:02.241 1ea53e10-cf0d-428a-839a-eb98fa4eb8a4 6.8 FAHRENHEIT ]
SensorReading [ 2025-01-02 14:22:07.245 1ea53e10-cf0d-428a-839a-eb98fa4eb8a4 28.6 FAHRENHEIT ]
SensorReading [ 2025-01-02 14:22:12.248 1ea53e10-cf0d-428a-839a-eb98fa4eb8a4 15.3 FAHRENHEIT ]
SensorReading [ 2025-01-02 14:22:17.251 1ea53e10-cf0d-428a-839a-eb98fa4eb8a4 38.6 FAHRENHEIT ]
```

🚀 Leverage the power of Spring Cloud Stream to build robust and scalable data production pipelines with ease! 🚀

