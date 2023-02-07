# Spring Boot 3 - Temporary Workaround for Spring Boot Auto-Configuration for Solace JMS

## Disclaimer

This workaround is for temporary use only. It should not be used in production, but it should be stable enough to use in development.

In regard to the product, Spring Boot Auto-Configuration for the Solace JMS, the workaround shown in this sample is expected to only be compatible with version 4.3.0. There should be no expectation for the workaround to be supported in any future releases of this starter.

Similarly, this workaround was only verified to work with the specific dependencies used in this sample. The workaround might not work if you were to diverge or upgrade these dependencies.

## Overview

The sample in this repository illustrates how to partially downgrade a Spring Boot `3.0.1` application to work with Spring Boot Auto-Configuration for Solace JMS `4.3.0`, which uses Javax JMS. To do this, this sample downgrades JMS-related Spring dependencies to use their previous major versions.

The core of the workaround is as follows:

1. Use `spring-boot-starter-parent` parent POM version `3.0.1`
2. Add `sol-jms` version `10.17.0`
3. Add `solace-jms-spring-boot-autoconfigure` version `4.3.0` and exclude the following transitive dependencies:
    * `spring-boot-autoconfigure`
    * `spring-boot-starter-logging`
    * `spring-jms`
    * `spring-boot-configuration-processor`
4. Add the following downgraded dependencies to your application:
    * `spring-integration-jms` version `5.5.16`
    * `spring-jms` version `5.3.24`
    * `javax.annotation-api` version `1.3.2`
5. Add the following Spring Boot `3.0.1` dependencies:
    * `spring-boot-starter` version `3.0.1`
    * `spring-messaging` version `6.0.3`
6. Add all other Spring dependencies compatible with Spring Boot version `3.0.1` (`spring-boot-configuration-processor`, `etc).

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
