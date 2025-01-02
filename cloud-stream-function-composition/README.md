# Function Composition

The `FunctionComposition` application is a Spring Boot application that demonstrates function composition in Spring Cloud Stream. It shows how to chain multiple functions together to process messages.

## Requirements

To run this sample, you will need to have installed:

Java 17 or Above

## Intent and Purpose

The purpose of this application is to showcase:
- How to define and chain multiple functions in Spring Cloud Stream.
- How to configure function bindings and composition in `application.yml`.
- How Spring Cloud Stream handles type conversion between functions.

## Code Overview

### Main Class

The main class `FunctionComposition` is annotated with `@SpringBootApplication` to enable Spring Boot's auto-configuration.

```java
@SpringBootApplication
public class FunctionComposition {
  private static final Logger log = LoggerFactory.getLogger(FunctionComposition.class);

  public static void main(String[] args) {
    SpringApplication.run(FunctionComposition.class);
  }
}
```

## Function Definitions
The application defines three functions: **preProcess**, **process**, and **postProcess**.

### PreProcess Function
The preProcess function takes a ```Message<Object>``` as input and returns a ```String```.

```java
@Bean
public Function<Message<Object>, String> preProcess() {
  return input -> {
    log.info("preProcess: " + input);
    return "{\"preProcess\":\"says hello\"}";
  };
}
```

### Process Function
The process function takes a ```JsonNode``` as input and returns an ```Object```.

```java
public Function<JsonNode, Object> process() {
  return input -> {
    return "Hello World from process";
  };
}
```

### PostProcess Function
The *postProcess* function takes an ```Object``` as input and returns a ```String```.

```java
@Bean
public Function<Object, String> postProcess() {
  return input -> {
    log.info("postProcess: " + input);
    return "postProcess is complete!";
  };
}
```

## Function Composition
The functions are independent of each other and can be reused. The bindings and composition are set up in ```application.yml```.

## Running the Application
To run the application, use the following command:

```sh
mvn clean spring-boot:run
```

This will start the Spring Boot application and you will see the logs for each function being executed in sequence.

```log
2025-01-02T16:43:16.851+05:30  INFO 26761 --- [pool-3-thread-1] c.s.s.spring.scs.FunctionComposition     : preProcess: GenericMessage [payload=byte[11], headers={solace_senderTimestamp=1735816396780, solace_expiration=0, solace_destination=pub/sub/plus/1, solace_replicationGroupMessageId=rmid1:3b202-25d86a922c7-00000000-000001f2, deliveryAttempt=1, solace_isReply=false, solace_timeToLive=0, solace_receiveTimestamp=0, target-protocol=kafka, acknowledgmentCallback=com.solace.spring.cloud.stream.binder.inbound.acknowledge.JCSMPAcknowledgementCallback@ec473380, solace_discardIndication=false, solace_dmqEligible=false, solace_priority=-1, solace_redelivered=false, id=8f1b3544-4272-cff0-5f9c-c644ff048e5d, contentType=application/json, solace_senderId=Try-Me-Pub/solclientjs/chrome-131.0.0-OSX-10.15.7/1843312849/0003, timestamp=1735816396826}]
2025-01-02T16:43:16.873+05:30  INFO 26761 --- [pool-3-thread-1] c.s.s.spring.scs.FunctionComposition     : process: {"preProcess":"says hello"}
2025-01-02T16:43:16.874+05:30  INFO 26761 --- [pool-3-thread-1] c.s.s.spring.scs.FunctionComposition     : postProcess: Hello World from process
```

## Conclusion

This application demonstrates how to use Spring Cloud Stream to chain multiple functions together to process messages. It highlights the configuration of function bindings and the type conversion between functions.

🚀 Leverage the power of Spring Cloud Stream to build robust and scalable data processing pipelines with ease! 🚀

