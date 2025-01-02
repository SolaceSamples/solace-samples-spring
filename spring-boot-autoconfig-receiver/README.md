# spring-boot-autoconfig-receiver

The `SpringBootReceiver` application is a Spring Boot application that demonstrates how to receive messages from a JMS queue using Spring's `JmsListener`. 

## Intent and Purpose

The purpose of this application is to showcase how to:
- Configure a Spring Boot application to receive messages a queue.
- Use `JmsListener` to listen for messages from a queue.

## Code Overview

### Main Class

The main class `SpringBootReceiver` is annotated with `@SpringBootApplication` to enable Spring Boot's auto-configuration support.

```java
@SpringBootApplication
public class SpringBootReceiver {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootReceiver.class, args);
    }
}
```

### Receiving Messages

```java
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
```

### Running the Application

Before you run the receive application, 
1. Do create a queue by name ```SpringTestQueue``` 
2. Run the ```spring-boot-autoconfig-receiver``` application to populate messages on the queue.

To run the receiver application, use the following command:

```log
mvn clean spring-boot:run
```

This will start the Spring Boot application and you will see messages retrieved from the queue in the console output.

```log
Message Received at 2025-01-02 15:26:36.323 with message content of: Hello World 1735811281313
Message Received at 2025-01-02 15:26:36.370 with message content of: Hello World 1735811286313
Message Received at 2025-01-02 15:26:36.374 with message content of: Hello World 1735811291313
Message Received at 2025-01-02 15:26:36.378 with message content of: Hello World 1735811296313
Message Received at 2025-01-02 15:26:36.382 with message content of: Hello World 1735811301313
Message Received at 2025-01-02 15:26:36.385 with message content of: Hello World 1735811306313
Message Received at 2025-01-02 15:26:36.388 with message content of: Hello World 1735811311313
Message Received at 2025-01-02 15:26:36.391 with message content of: Hello World 1735811316313
Message Received at 2025-01-02 15:26:36.395 with message content of: Hello World 1735811321313
Message Received at 2025-01-02 15:26:36.399 with message content of: Hello World 1735811326313
```

## Conclusion
This application demonstrates how to use Spring Boot and ```JmsListener``` to receive messages from a queue. 
