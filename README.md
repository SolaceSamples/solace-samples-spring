# Spring Boot 3 - Temporary Workaround for Spring Boot Auto-Configuration for Solace JMS

## Disclaimer

This workaround is provided as a temporary solution to allow Solace customers to use Spring Boot 3.x with Solace JMS. As the workaround requires partially downgrading some components of the Spring Boot release (see below), thorough testing should be done to application code relying on this workaround. Solace recommends replacing the workaround in your application once a permanent solution is released.

In regard to the product, Spring Boot Auto-Configuration for the Solace JMS, the workaround shown in this sample is expected to only be compatible with version 4.3.0. There should be no expectation for the workaround to be supported in any future releases of this starter.

Similarly, this workaround was only verified to work with the specific dependencies used in this sample. The workaround might not work if you were to diverge or upgrade these dependencies.

## Overview

The sample in this repository illustrates how to partially downgrade a Spring Boot `3.0.1` application to work with Spring Boot Auto-Configuration for Solace JMS `4.3.0`, which uses Javax JMS. To do this, this sample downgrades JMS-related Spring dependencies to use their previous major versions.

The core of the workaround involves changes to your POM as follows:

```xml
<parent>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-parent</artifactId>
     <version>3.0.1</version>
     <relativePath/>
</parent>

<dependencies>
   <!-- Downgrade Spring Integration JMS -->
   <dependency>
      <groupId>org.springframework.integration</groupId>
      <artifactId>spring-integration-jms</artifactId>
      <version>5.5.16</version>
   </dependency>

   <dependency>
      <groupId>com.solacesystems</groupId>
      <artifactId>sol-jms</artifactId>
      <version>10.17.0</version>
   </dependency>

   <!-- exclude from solace autoconfiguration all incompatible to Spring 6/Springboot 3 artifacts -->
   <dependency>
      <groupId>com.solace.spring.boot</groupId>
      <artifactId>solace-jms-spring-boot-autoconfigure</artifactId>
      <version>4.3.0</version>
      <exclusions>
         <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
         </exclusion>
         <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
         </exclusion>
         <exclusion>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jms</artifactId>
         </exclusion>
         <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
         </exclusion>
      </exclusions>
   </dependency>
   
   <!-- Downgrade Spring JMS -->
   <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-jms</artifactId>
      <version>5.3.24</version>
      <scope>compile</scope>
   </dependency>
   
   <!-- add support for javax annotations -->
   <dependency>
      <groupId>javax.annotation</groupId>
      <artifactId>javax.annotation-api</artifactId>
      <version>1.3.2</version>
      <scope>compile</scope>
   </dependency>
   
   <!--
   All remaining dependencies below all uses the dependencies for Spring Boot 3.0.x
   -->
   
   <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter</artifactId>
      <version>3.0.1</version>
      <scope>compile</scope>
   </dependency>
   
   <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-messaging</artifactId>
      <version>6.0.3</version>
      <scope>compile</scope>
   </dependency>
</dependencies>
```

For a concrete example, [please see this sample's POM file](./spring-boot-autoconfig-receiver/pom.xml).

## Running the Sample

This sample will illustrate the functionality of the following features using the downgraded JMS:

* Publishing messages using a `JmsTemplate`.
* Non-transactional message consumption using `@JmsListener`.
* Transactional message consumption using `@JmsListener`.
* (optional) Using a JNDI connection factory and JNDI destinations.

First, in your PubSub+ broker, provision the following queues and add the following subscriptions as required by the properties of this sample's [application.properties file](./spring-boot-autoconfig-receiver/src/main/resources/application.properties):

| Queue         | Subscription    |
|---------------|-----------------|
| `trigger`     | `trigger_topic` |
| `orders`      | `order_topic`   |
| `orders_poll` | `order_topic`   |

Optionally, you may use JNDI by uncommenting the `sample.solace.jndi-name` config option. In which case, the `/jms/cf/default` connection will be used, and all queues and topics will be resolved using JNDI.

Now to run the sample, go into the project directory and run the sample using maven:

```shell
cd spring-boot-autoconfig-receiver
mvn spring-boot:run
```

To send a message to be consumed by this sample, send the following GET request:

```shell
curl -X GET localhost:8090/order/send
```

This sample will then perform the following actions:

1. Send a trigger message to the `trigger_topic` topic.
    * Because of the `trigger_topic` subscription you added earlier, the PubSub+ broker will route the trigger message to the `trigger` queue.
2. Transactionally consume the trigger message from the `trigger` queue and throw an exception.
    * Causes the transaction to rollback and PubSub+ to redeliver the trigger message.
3. Upon redelivery of the trigger message, the sample will send an order message to the `order_topic` topic, and successfully commits the transaction with the trigger message.
    * Because of the `order_topic` subscriptions you added earlier, the PubSub+ broker will route the order message to both the `orders` and `orders_poll` queue.
4. Consumes the order message from the `orders` queue and logs its contents.

Notice that there is still an order message remaining in the `orders_poll` queue. To consume it, send the following GET request:

```shell
curl -X GET localhost:8090/order/poll
```

The returned response will contain the order message.

## Exploring the Samples

### Setting up your preferred IDE

Using a modern Java IDE provides cool productivity features like auto-completion, on-the-fly compilation, assisted refactoring and debugging which can be useful when you're exploring the samples and even modifying the samples. Follow the steps below for your preferred IDE.

This repository uses Maven projects. If you would like to import the projects into your favorite IDE you should be able to import them as Maven Projects. For examples, in eclipse choose "File -> Import -> Maven -> Existing Maven Projects -> Next -> Browse for your repo -> Select which projects -> Click Finish"

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Authors

See the list of [contributors](https://github.com/SolaceSamples/solace-samples-spring/contributors) who participated in this project.

## License

This project is licensed under the Apache License, Version 2.0. - See the [LICENSE](LICENSE) file for details.

## Resources

For more information try these resources:

- [Tutorials](https://tutorials.solace.dev/)
- The Solace Developer Portal website at: [Developer Portal](http://solace.com/developers)
- Check out the [Solace blog](https://solace.com/blog/category/developers/) for other interesting discussions around Solace technology
- Follow our Developer Advocates on Twitter for development tips & to keep up with the latest Solace news! [@SolaceDevs](https://twitter.com/solacedevs)
