---
layout: tutorials
title: Spring Boot Autoconfig (JMS)
summary: Learn how to use Spring Boot Autoconfig with JMS & Solace PubSub+ 
icon: spring-boot.svg
links:
    - label: SpringBootSender
      link: /blob/master/src/main/java/com/solace/samples/spring/SpringBootSender.java
    - label: SpringBootReceiver
      link: /blob/master/src/main/java/com/solace/samples/spring/SpringBootReceiver.java
---

This tutorial will introduce you to the fundamentals of connecting an JMS client to Solace Messaging using Spring Boot with Autoconfigure. Spring Boot auto-configuration attempts to automatically configure your Spring application based on the jar dependencies that you have added.
- [Spring Boot Autoconfigure Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-auto-configuration.html)

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

The goal of this tutorial is to demonstrate how to use Spring Boot Autoconfigure to exchange JMS events using PubSub+
This tutorial will show you:

* How to create an app that will receive JMS messages.
* How to create an app that will send JMS messages.

## Spring Boot Introduction
The Spring Boot project makes it easy to create production grade Spring based Applications. Instead of having to manually inject each dependency Spring Boot takes an opinionated view that gets you started more quickly. More information can be found at the link below.
1. [Spring Boot Project](https://spring.io/projects/spring-boot){:target="_blank"}

## Java Messaging Service (JMS) Introduction

The Java Message Service (JMS) API is a Java message-oriented middleware API for sending messages between two or more clients. It is very commonly used by Java Developers to build event driven applications. More information can be found at the links below. 

1.  [https://en.wikipedia.org/wiki/Java_Message_Service](https://en.wikipedia.org/wiki/Java_Message_Service){:target="_blank"}
2.  [Basic JMS API Concepts](https://docs.oracle.com/javaee/6/tutorial/doc/bncdx.html){:target="_blank"}

{% include_relative assets/solaceMessaging.md %}

## Create Your Queue 
In order for us to run our sender/receiver we need to first create the Queue that they will send/receive from. 
We will be creating a queue named "SpringTestQueue". 

* If using Solace Cloud, create the queue by following these instructions: [Create Queue](https://solace.com/cloud-learning/group_getting_started/ggs_queue.html)
* If using a local docker instance login to the PubSub+ Manager at localhost:8080/#/login. Once logged in choose your message VPN, click "Queues" on the left, and click the "+Queue" button on the top right to create your queue. 
* If using an appliance, ask your administrator to create the queue for you.

* You can also create a queue from the CLI. [Learn how to login to the cli here](https://docs.solace.com/Solace-CLI/Using-Solace-CLI.htm) 
```
solace(configure)# message-spool message-vpn <vpn-name>
solace(configure/message-spool)# create queue SpringTestQueue
```

## Getting the Source
Clone the GitHub repository containing the Solace samples.

```
git clone {{ site.repository }}
cd {{ site.repository | split: "/" | last }}
```

## Project Setup
You should now be in a directory that itself contains multiple directories.
The following 2 will be used in this tutorial: 
* spring-boot-autoconfig-sender
* spring-boot-autoconfig-receiver

The following sections will run the apps from the command line, but if you prefer to use an IDE the projects can be imported as "Maven Projects"

## Analyze the Maven Dependencies
Using your favorite text editor open the spring-boot-autoconfig-sender/pom.xml file

```
cd spring-boot-autoconfig-sender
vim pom.xml
```

This file defines the dependencies needed for our sender app to build & run, but they are also the same for our receiver app.
Note that the app is using the spring-boot-starter-parent. This starts the app off by including common Spring Boot dependencies. 
``` xml
    <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>2.1.4.RELEASE</version>
            <relativePath /> 
    </parent>
```

Also note that the dependency below is what enables us to use Solace PubSub+ as our JMS provider. It also includes the dependency to enable autoconfiguration based on properties in our Spring Boot Config file.

``` xml
    <dependency>
            <groupId>com.solace.spring.boot</groupId>
            <artifactId>solace-jms-spring-boot-starter</artifactId>
            <version>1.0.0</version>
    </dependency>
```

## Learn about the Receiver

### SpringBootReceiver.java
Open a new console/terminal if needed. 
Open the SpringBootReceiver.java file in the "spring-boot-autoconfig-receiver" project.
This class shows how simple it is to create a Spring Boot app that receives events from a PubSub+ queue. 
The class will setup a JmsListener on the "SpringTestQueue" which should already be created. 

A few things to take note of:
* The [@SpringBootApplication](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-using-springbootapplication-annotation.html) annotation enables auto-configuration and component scanning
* In our code below this auto-configuration & scanning includes identifying the @JmsListener annotation specified on our "handle" method. When it identifies that annotation Spring knows that it needs a JMS ConnectionFactory so it looks at the Maven dependencies and discovers that the libraries provided by the "solace-jms-spring-boot-autoconfigure" dependency do indeed provide one. Spring then searches the properties available in the Spring Boot properties file, which we'll look at next, to see that it has the properties it needs to automatically create & inject the ConnectionFactory & related objects. 
* The @JmsListener annotation sets the JMS destination name that we want to listen on and also identifies the "handle" method as the method to be executed upon receipt of a message.  

``` java
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
```

### application.properties
This is the Spring Boot Configuration file. Note that the 4 properties below start with "solace.jms" and are what allows autoconfig to automate connection to our event broker. If not using the default connection information you'll need to change your properties below. 

``` properties
solace.jms.host=smf://localhost:55555
solace.jms.msgVpn=default
solace.jms.clientUsername=default
solace.jms.clientPassword=default
```

### Run the Receiver
Now it's time to run the app.
Run from the command line using maven like seen below

```
mvn spring-boot:run
```

When the app is started you should see a message on the console that contains "Started SpringBootReceiver". 
Leave the receiver running so it can receive messages sent by our Sender in the next section.

## Learn about the Sender

### SpringBootSender.java
Open a new console/terminal if needed. 
Open the SpringBootSender.java file in the "spring-boot-autoconfig-sender" project.
This class shows how simple it is to create a Spring Boot app that sends events to a PubSub+ queue. 
The class will send an event every 5 seconds

A few things to take note of:
* The [@SpringBootApplication](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-using-springbootapplication-annotation.html) annotation enables auto-configuration and component scanning
* In our code below this auto-configuration & scanning includes autowiring the JmsTemplate object. This includes creating a connection factory and giving us the ability to send messages!
* Note that we are also using the @EnableScheduling annotation in conjunction with the @Scheduled annotation to execute the "sendEvent" method every 5 seconds. 
* One last thing to note is that we are using the @PostConstruct annotation to update our jmsTemplate to cache our connections instead of creating a new one for every message sent. 

```java
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

```

### application.properties
This is the Spring Boot Configuration file. Note that the 4 properties below start with "solace.jms" and are what allows autoconfig to automate connection to our event broker. If not using the default connection information you'll need to change your properties below. 

``` properties
solace.jms.host=smf://localhost:55555
solace.jms.msgVpn=default
solace.jms.clientUsername=default
solace.jms.clientPassword=default
```

### Run the Sender
Now it's time to run the app.
Run from the command line using maven like seen below

```
mvn spring-boot:run
```

When the app is started you should see a message on the console that contains "Started SpringBootSender". 
You should see the sender send a message every 5 seconds. 
At this point you should also see the receiver receiving the messages. 

## Takeaway
Spring Boot makes it super simple to quickly develop Spring Applications. And when used with Autoconfig & Solace PubSub+ it will automatically discover your configurations & connect to the PubSub+ service! Note that you didn't have to manually create all the boilerplate JMS objects that you may be used to, such as Connection Factories, Message Producers and Sessions. This ease of use allows for consistency across your applications and the ability to focus your time on achieving business goals.
