# Generated Queue & Error Queue Names

This Spring Cloud Stream sample will show how to generate custom names for queues and error queues using Spring configuration. We will also examine the attributes from [SolaceCommonProperties](https://github.com/SolaceProducts/solace-spring-cloud/blob/master/solace-spring-cloud-stream-binder/solace-spring-cloud-stream-binder-core/src/main/java/com/solace/spring/cloud/stream/binder/properties/SolaceCommonProperties.java) and [SolaceConsumerProperties](https://github.com/SolaceProducts/solace-spring-cloud/blob/master/solace-spring-cloud-stream-binder/solace-spring-cloud-stream-binder-core/src/main/java/com/solace/spring/cloud/stream/binder/properties/SolaceConsumerProperties.java) that helps in the name generation.

## Requirements

To run this sample, you will need to have installed:

Java 17 or Above

## Custom Queue & Error Queue names

Solace binder supports custom names for queues and error queues using `queueNameExpression` and `errorQueueNameExpression` properties. 

The `queueNameExpression` and `errorQueueNameExpression` follow [SpEL](https://docs.spring.io/spring-framework/docs/4.3.12.RELEASE/spring-framework-reference/html/expressions.html) expression. Users can provide any valid SpEL expression to generate custom queue names. Valid expressions evaluate against the following context:

The default SpEL expression for creating the consumer group’s queue name is here.
```
'scst/' + (isAnonymous ? 'an/' : 'wk/') 
+ (group?.trim() + '/') + 'plain/' 
+ destination.trim().replaceAll('[*>]', '_')
```

You can also specify a literal name as the queue name in `queueNameExpression` property. The SpEL expects literal names to be quoted, and the quotes need to be escaped.

```
queueNameExpression: ‘’’solace/just/a/literal/queuename’’’
```

## Running the application

Make sure to update the Solace Broker connection details with the appropriate host, msgVpn, client username, and password in `spring.cloud.stream.binders.solace-broker.environment` settings.

```
cd generated-queue-names
mvn clean spring-boot:run
```
This will start the Spring Boot application.

Based on the configuration, you can see the generated names for the queue and error queue.

<p align="center"><img width="auto" alt="auth" src="images/generated-names-1.jpg"></p>

Generated Queue name:

<p align="center"><img width="640" alt="auth" src="images/generated-names-2.jpg"></p>

Generated Error Queue name:

<p align="center"><img width="640" alt="auth" src="images/generated-names-3.jpg"></p>

Generated Literal Queue name (hard-coded):

<p align="center"><img width="640" alt="auth" src="images/generated-names-4.jpg"></p>

🔥 Yes, Now you can name your queue and error queue names as you wish! 🔥