---
layout: tutorials
title: Spring Cloud Streams
summary: Learn how to use Spring Cloud Streams w/ the PubSub+ Binder
icon: spring-cloud.svg
links:
    - label: Source Example
      link: /blob/master/src/main/java/com/solace/samples/spring/scs/FarhenheitTempSource.java
    - label: Sink Example
      link: /blob/master/src/main/java/com/solace/samples/spring/scs/TemperatureSink.java
    - label: Processor Example
      link: /blob/master/src/main/java/com/solace/samples/spring/scs/ConvertFtoCProcessor.java
---

This tutorial will introduce you to the fundamentals of using Spring Cloud Streams with the Solace PubSub+ Binder. You will create a Source (sending app), a Sink (receiving app), and a Processor (combination of a source & a sink). The apps will exchange events using a PubSub+ Event Broker

## Assumptions

This tutorial assumes the following:

*   You are somewhat familiar with Java 
*   You have Maven 3.5.3 or higher (ensure it's on your PATH) [Install steps here](https://maven.apache.org/install.html)
*   You have JDK 1.8 (ensure your PATH & JAVA_HOME are updated as needed)
*   You have access to Solace messaging with the following configuration details:
    *   Connectivity information for a Solace message-VPN
    *   Enabled client username and password

One simple way to get access to Solace messaging quickly is to create a messaging service in Solace Cloud [as outlined here]({{ site.links-solaceCloud-setup}}){:target="_top"}. You can find other ways to get access to Solace messaging below.

## Goals

The goal of this tutorial is to demonstrate the use of Spring Cloud Streams with the Solace PubSub+ Binder. This tutorial will show you:

*   How to create a Spring Cloud Streams Source (sending app) to send events into PubSub+
*   How to create a Spring Cloud Streams Sink (receiving app) to receive events from PubSub+
*   How to create a Spring Cloud Streams Processor (sending & receiving app) to process events using PubSub+

## Spring Cloud Streams (SCS) Introduction

“Spring Cloud Stream is a framework for building highly scalable event-driven microservices connected with shared messaging systems."
It is based on Spring Boot, Spring Cloud, Spring Integration and Spring Messaging
Solace PubSub+ is a partner maintained binder implementation for Spring Cloud Streams. 
1. Spring Cloud Stream Project Home: [https://spring.io/projects/spring-cloud-stream](https://spring.io/projects/spring-cloud-stream){:target="_blank"}
2. The Reference Guide for that current version is available [here](https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle){:target="_blank"}
3. PubSub+ Binder [https://github.com/SolaceProducts/spring-cloud-stream-binder-solace](https://github.com/SolaceProducts/spring-cloud-stream-binder-solace){:target="_blank"}

{% include_relative assets/solaceMessaging.md %}


## Getting the Source
Clone the GitHub repository containing the Solace samples.

```
git clone {{ site.repository }}
cd {{ site.repository | split: "/" | last }}
```

## Project Setup
You should now be in a directory that itself contains multiple directories. 
The following 4 will be used in this tutorial: 
* cloud-streams-processor
* cloud-streams-sink
* cloud-streams-source
* spring-samples-datamodel

The following sections will run the apps from the command line, but if you prefer to use an IDE the projects can be imported as "Maven Projects" 

### Install the data model
In order to compile/run the actual applications the data model must first be installed to your local maven repo. 

```
cd spring-samples-datamodel
mvn clean install
```

### Analyze the Maven Dependencies
Using your favorite text editor open the cloud-streams-sink/pom.xml file

```
cd cloud-streams-sink
vim pom.xml
```

This file defines the dependencies needed for our Sink app to build & run, but they are also the same for our Source and Processor apps. 
Note that the app is actually a Spring Boot Application & is based off the spring-boot-starter-parent. 
```
    <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>2.1.4.RELEASE</version>
            <relativePath /> <!-- lookup parent from repository -->
    </parent>
```


Also note that the dependency below is what enables us to use the Solace PubSub+ Binder. 
```
    <dependency>
            <groupId>com.solace.spring.cloud</groupId>
            <artifactId>spring-cloud-starter-stream-solace</artifactId>
            <version>1.1.0</version>
    </dependency>
```


## Learn about the Sink
### TemperatureSink.java
Open the TemperatureSink.java file in the "cloud-streams-sink" project. 
This class shows how simple it is to write a Spring Cloud Streams app that consumes events from PubSub+.

A few things to take note of: 
* The [@SpringBootApplication](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-using-springbootapplication-annotation.html) annotation enables auto-configuration and component scanning
* The @EnableBinding(Sink.class) annotation tells us that we are creating a Spring Cloud Streams Sink application and enables the Input channel on the Sink binding interface. This Sink's input channel will connect to our messaging system at run time.
* The @StreamListener annotation defines which method should be invoked when an event is received on our Sink.INPUT channel. 

``` java
@SpringBootApplication
@EnableBinding(Sink.class)
public class TemperatureSink {
	private static final Logger log = LoggerFactory.getLogger(TemperatureSink.class);

	public static void main(String[] args) {
		SpringApplication.run(TemperatureSink.class, args);
	}

	// We define an INPUT to receive from
	@StreamListener(Sink.INPUT)
	public void sink(SensorReading reading) {
		log.info("Received: " + reading);
	}
}
```

### Sink:application.yml
Next let's take a look at the application.yml file. Note that an application.properties file could be used instead. 
Open the application.yml file in the "cloud-streams-sink" project.

A few things to take note of: 
* The spring.cloud.stream.bindings.input maps to the Sink.INPUT channel for our application. 
* Because a "group" is specified we are following the Spring Cloud Streams "Consumer Group" pattern; if a group was not specified the app would be using the Publish-Subscribe pattern. 
* Spring Cloud Streams will use the "local_solace" binder since it's the only one present; if multiple binders are present you can specify the binder on each binding. 
* Change your *host*, *msgVpn*, *clientUsername* & *clientPassword* to match your Solace Messaging Service. The host should be your "SMF URI".
* Notice the spring.cloud.steams.solace.bindings is where Solace specific configurations can be set; here we see an example where we are telling our queue to subscribe to the "sensor/temperature/>" topic. The ">" sign is a wildcard that allows us to receive any events sent to any topic that starts with "sensor/temperature/" We will be using it to receive events on "sensor/temperature/celsius" and "sensor/temperature/fahrenheit" topics.

``` yaml
spring:
  cloud:
    stream:
      bindings:
        input:
          destination: TEMPS.Q
          #The presence of "group" tells the binder to follow the "consumer group" pattern
          group: SINK
      binders:
        local_solace:
          type: solace
          environment:
            solace:
              java:
                host: tcp://localhost:55555
                msgVpn: default
                clientUsername: default
                clientPassword: default
      solace:
        bindings:
          input:
            consumer:
              queueAdditionalSubscriptions: sensor/temperature/>
```

### Sink:Run the app
Now it's time to run the app.
Run from the command line using maven like seen below

```
mvn spring-boot:run
```

You should see the application start; you will know it's started when the console has a line that contains "Started TemperatureSink in" 
Leave the app running for the remainder of the tutorial & don't worry that no events are being received yet. 

## Learn about the Source

### FahrenheitTempSource.java
Open a new console/terminal if needed. 
Open the FahrenheitTempSource.java file in the "cloud-streams-source" project. 
This class shows how simple it is to write a Spring Cloud Streams app that sends events to PubSub+.
The class is simulating an event source that emits a temperature, in Fahrenheit, every 5 seconds. 

A few things to take note of: 
* As before the [@SpringBootApplication](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-using-springbootapplication-annotation.html) annotation enables auto-configuration and component scanning
* The @EnableBinding(Source.class) annotation tells us that we are creating a Spring Cloud Streams Source application and enables the Output channel on the Source binding interface. This Source's output channel will connect to our messaging system at run time.
* The @InboundChannelAdapter annotation defines which method will be using the Source.OUTPUT channel. It is also enabling us to send a SensorReading event every 5 seconds. 

``` java
@SpringBootApplication
@EnableBinding(Source.class)
public class FahrenheitTempSource {
    private static final Logger log = LoggerFactory.getLogger(FahrenheitTempSource.class);

    private static final UUID sensorIdentifier = UUID.randomUUID();
    private static final Random random = new Random(System.currentTimeMillis());
    private static final int RANDOM_MULTIPLIER = 100;
    

        public static void main(String[] args) {
                SpringApplication.run(FahrenheitTempSource.class, args);
        }

    @InboundChannelAdapter(channel = Source.OUTPUT, poller = @Poller(fixedRate = "5000"))
    public SensorReading emitSensorReading() {
        double temperature = random.nextDouble() * RANDOM_MULTIPLIER;

        SensorReading reading = new SensorReading();
        reading.setSensorID(sensorIdentifier.toString());
        reading.setTemperature(temperature);
        reading.setBaseUnit(BaseUnit.FAHRENHEIT);
        
        log.info("Emitting " + reading);

        return reading;
    }
```

### Source:application.yml
Next let's take a look at the application.yml file. As stated earlier, an application.properties file could be used instead. 
Open the application.yml file in the "cloud-streams-source" project.

A few things to take note of: 
* The spring.cloud.stream.bindings.output maps to the Source.OUTPUT channel for our application; in this example we are sending to the "sensor/temperature/fahrenheit" topic. 
* Spring Cloud Streams will use the "local_solace" binder since it's the only one present; if multiple binders are present you can specify the binder on each binding. 
* Change your *host*, *msgVpn*, *clientUsername* & *clientPassword* to match your Solace Messaging Service. The host should be your "SMF URI".

``` yaml
spring:
  cloud:
    stream:
      bindings:
        output:
          destination: sensor/temperature/fahrenheit
          binder: local_solace
      binders:
        local_solace:
          type: solace
          environment:
            solace:
              java:
                host: tcp://localhost:55555
                msgVpn: default
                clientUsername: default
                clientPassword: default
```

### Source:Run the app
Now it's time to run the app.
Run from the command line using maven like seen below

```
mvn spring-boot:run
```

You should see the application start; you will know it's started when the console has a line that contains "Started FahrenheitTempSource in" 
After starting you should see the Source app emitting an event every 5 seconds. 
Note that if you look back at your Sink app that was deployed previously you should now see it receiving the events that are being emitted. 

At this point you have now implemented a sending & receiving app using Spring Cloud Streams and PubSub+ !!
Leave the app running for the remainder of the tutorial.

## Learn about the Processor 

### ConvertFtoCProcessor.java
Open a new console/terminal if needed. 
Open the ConvertFtoCProcessor.java file in the "cloud-streams-processor" project. 
This class shows how simple it is to write a Spring Cloud Streams app that receives, processesses & sends to PubSub+.
The class is a processor which receives SensorReadings in Fahrenheit from one topic, converts them to Celsius and publishes the updated SensorReadings to a Celsius topic

A few things to take note of: 
* As before the [@SpringBootApplication](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-using-springbootapplication-annotation.html) annotation enables auto-configuration and component scanning
* The @EnableBinding(Processor.class) annotation tells us that we are creating a Spring Cloud Streams Processor application and enables the Input & Output channels on the Processor binding interface. This Prcoessor's input & output channels will be bound to our messaging system at run time.
* The @StreamListener annotation defines which method should be invoked when an event is received on our Processor.INPUT channel. 
* The @SendTo annotation defines that returned objects from the method should be sent to the Processor.OUTPUT channel. 

``` java
@SpringBootApplication
@EnableBinding(Processor.class)
public class ConvertFtoCProcessor {
	
	private static final Logger log = LoggerFactory.getLogger(ConvertFtoCProcessor.class);
	
	public static void main(String[] args) {
		SpringApplication.run(ConvertFtoCProcessor.class, args);
	}
	
	@StreamListener(Processor.INPUT)
	@SendTo(Processor.OUTPUT)
	public SensorReading handle(SensorReading reading) {
		
		log.info("Received: " + reading);
        
		double temperatureCelsius = (reading.getTemperature().doubleValue() - 32) * 5 / 9;
                reading.setTemperature(temperatureCelsius);
                reading.setBaseUnit(SensorReading.BaseUnit.CELSIUS);

		log.info("Sending: " + reading);
		return reading;
	}
}
```

### Processor:application.yml
Next let's take a look at the application.yml file. As stated earlier, an application.properties file could be used instead. 
Open the application.yml file in the "cloud-streams-processor" project.

A few things to take note of: 
* The spring.cloud.stream.bindings now includes both an "input" and an "output" binding. These maps to our Processor.INPUT & Processor.OUTPUT channels respectively; note that our output destination will be sending to the "sensor/temperature/celsius" topic. 
* Spring Cloud Streams will use the "local_solace" binder since it's the only one present; if multiple binders are present you can specify the binder on each binding. 
* Change your *host*, *msgVpn*, *clientUsername* & *clientPassword* to match your Solace Messaging Service. The host should be your "SMF URI".
* Notice the spring.cloud.steams.solace.bindings is where Solace specific configurations can be set; here we see an example where we are telling our input bindings queue to subscribe to the "sensor/temperature/fahrenheit" topic.

``` yaml
spring:
  cloud:
    stream:
      default-binder: local_solace
      bindings:
        input:
          destination: TEMPS.Q
          group: PROCESSOR
        output:
          destination: sensor/temperature/celsius
      binders:
        local_solace:
          type: solace
          environment:
            solace:
              java:
                host: tcp://localhost:55555
                msgVpn: default
                clientUsername: default
                clientPassword: default
                connectRetries: -1
                reconnectRetries: -1
      solace:
        bindings:
          input:
            consumer:
              queueAdditionalSubscriptions: sensor/temperature/fahrenheit
```

### Processor:Run the app
Now it's time to run the app.
Run from the command line using maven like seen below

```
mvn spring-boot:run
```

You should see the application start; you will know it's started when the console has a line that contains "Started ConvertFtoCProcessor in" 
After starting you should see the Processor app start to process the fahrenheit events sent by the Source that is still running from before. 

Note that if you look back at your Sink app you should now see it receiving both Fahrenheit & Celsius events. This is because it is subscribed to the "sensor/temperature/>" 
Fahrenheit events are being received from the initial Source app from the "sensor/temperature/fahrenheit" topic and Celsius events are being received from the Processor app from the "sensor/temperature/celsius" topic. 


## Takeaway
Spring Cloud Streams makes it super simple to develop event-driven microservices & applications! Note that you did not have to learn any messaging APIs in order to use Spring Cloud Streams. It allowed you to create an entire event creation, processing and receiving chain without having to use Messaging/Eventing APIs which allows your to focus on your business goals. 

