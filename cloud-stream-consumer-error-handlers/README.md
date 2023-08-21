# Consumer Error Handling

Errors happen, and Spring Cloud Stream provides several flexible mechanisms to deal with them. By default, if no additional  configuration is provided, the messaging system drops the failed message. While acceptable in some cases, for most cases, it is not, and we need some recovery mechanism to avoid message loss.

By design, Whenever a Message handler (function) throws an exception, it is propagated back to the binder, and the binder subsequently propagates the error back to the messaging system. Typically, when the messaging 

In the event you want to add some additional error handling (i.e., send notification, write to database etc), the Spring Cloud Stream binder for Solace PubSub+ supports both binder level error handlers and a default error handler, configurable via application settings. These error handlers are Spring Cloud Stream Consumers that are specifically designed to accept `ErrorMessage`.

Here is how you can specify a binder-level error handler.

```
...
bindings:
  functionOne-in-0:
    error-handler-definition: binderSpecificErrorHandler
    destination: solace/function/one
    group: errorhandler
...
```

And, a default error handle can be specified as follows

```
spring:
  cloud:
    function:
      definition: functionOne;functionTwo;binderSpecificErrorHandler;defaultErrorHandler;
    stream:
      default:
        error-handler-definition: defaultErrorHandler
...
```


NOTE: When declaring function-based error handler you MUST define spring-cloud-function-definition to identify your bindings, since you are effectively declaring another function that will be available in Function Catalog.


## Requirements

To run this sample, you will need to have installed:

Java 17 or Above

## Code Tour

In the Consumer Error Handlers application, review the function code. Whenever an error occurs in the binder's message handler function - this Consumer function will be automatically handed with the failing `Message` as `ErrorMessage` which wraps the original message.

```
	@Bean
	public Consumer<ErrorMessage> binderSpecificErrorHandler() {
		return message -> {
			// ErrorMessage received on a binder
			log.info("Received error message on binder-specific error handler");
			log.info("Original Message: " + message.getOriginalMessage());
		};
	}
```

Similarly, a default error handler can be defined as well. This would be called on binders that does not have a registered error handler.

```
	@Bean
	public Consumer<ErrorMessage> defaultErrorHandler() {
		return message -> {
			// ErrorMessage received on default error handler
			log.info("Received error message on default error handler");
			log.info("Original Message: " + message.getPayload());
		};
	}

```

Upon receiving the error message, you would be able to access the original message that failed by calling `getOriginalMessage` on the received `ErrorMessage`. At this point, you are free to carry out further actions like sending notification or writing to to a database etc.,

## Running the application

Make sure to update the Solace Broker connection details with the appropriate host, msgVpn, client username, and password in `spring.cloud.stream.binders.solace-broker.environment` settings.

```
cd cloud-stream-consumer-error-handlers
mvn clean spring-boot:run
```
This will start the Spring Boot application.

### Testing Binder-specific Error Handler
Publish the trigger message on the topic `solace/function/one` programmatically or using the Publisher tool in the `Try Me!` utility of the Solace Broker console. 


In the terminal, you can see the binder-specific error handler receiving an `ErrorMessage`.
```
2023-08-21T16:10:50.748+05:30  INFO 88595 --- [ool-64-thread-1] c.s.s.s.scs.ConsumerErrorHandlerDemo     : Received message on binding <functionOne-in-0>: Hello world!
2023-08-21T16:10:50.752+05:30  WARN 88595 --- [ool-64-thread-1] c.s.s.c.s.b.i.JCSMPInboundChannelAdapter : Failed to consume a message from destination queueOne - attempt 1
2023-08-21T16:10:51.754+05:30  INFO 88595 --- [ool-64-thread-1] c.s.s.s.scs.ConsumerErrorHandlerDemo     : Received message on binding <functionOne-in-0>: Hello world!
2023-08-21T16:10:51.754+05:30  WARN 88595 --- [ool-64-thread-1] c.s.s.c.s.b.i.JCSMPInboundChannelAdapter : Failed to consume a message from destination queueOne - attempt 2
2023-08-21T16:10:53.756+05:30  INFO 88595 --- [ool-64-thread-1] c.s.s.s.scs.ConsumerErrorHandlerDemo     : Received message on binding <functionOne-in-0>: Hello world!
2023-08-21T16:10:53.756+05:30  WARN 88595 --- [ool-64-thread-1] c.s.s.c.s.b.i.JCSMPInboundChannelAdapter : Failed to consume a message from destination queueOne - attempt 3
2023-08-21T16:10:53.769+05:30 ERROR 88595 --- [ool-64-thread-1] o.s.integration.handler.LoggingHandler   : org.springframework.messaging.MessageHandlingException: error occurred in message handler [org.springframework.cloud.stream.function.FunctionConfiguration$FunctionToDestinationBinder$1@a08d7e5d], failedMessage=GenericMessage [payload=byte[12], headers={solace_senderTimestamp=1692614450666, solace_expiration=0, solace_destination=solace/function/one, solace_replicationGroupMessageId=rmid1:24c78-197d007757c-00000000-00001ff0, deliveryAttempt=3, solace_isReply=false, solace_timeToLive=0, solace_receiveTimestamp=0, acknowledgmentCallback=com.solace.spring.cloud.stream.binder.inbound.acknowledge.JCSMPAcknowledgementCallback@ee13bdb5, solace_discardIndication=false, solace_dmqEligible=true, solace_priority=-1, solace_redelivered=false, id=e193ed52-0926-bd01-168c-671e7a02a95d, contentType=application/json, solace_senderId=Try-Me-Pub/solclientjs/chrome-116.0.0-OSX-10.15.7/0662633445/0001, timestamp=1692614450712}]
...
Caused by: java.lang.RuntimeException: Exception thrown
	at com.solace.samples.spring.scs.ConsumerErrorHandlerDemo.lambda$functionOne$0(ConsumerErrorHandlerDemo.java:46)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.invokeConsumer(SimpleFunctionRegistry.java:990)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.doApply(SimpleFunctionRegistry.java:701)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.apply(SimpleFunctionRegistry.java:550)
	at org.springframework.cloud.stream.function.PartitionAwareFunctionWrapper.apply(PartitionAwareFunctionWrapper.java:88)
	at org.springframework.cloud.stream.function.FunctionConfiguration$FunctionWrapper.apply(FunctionConfiguration.java:785)
	at org.springframework.cloud.stream.function.FunctionConfiguration$FunctionToDestinationBinder$1.handleMessageInternal(FunctionConfiguration.java:621)
	at org.springframework.integration.handler.AbstractMessageHandler.doHandleMessage(AbstractMessageHandler.java:105)
	... 32 more

2023-08-21T16:10:53.835+05:30  INFO 88595 --- [ool-64-thread-1] c.s.s.s.scs.ConsumerErrorHandlerDemo     : Received error message on binder-specific error handler
```

### Testing Binder-specific Error Handler
Publish the trigger message on the topic `solace/function/one` programmatically or using the Publisher tool in the `Try Me!` utility of the Solace Broker console. 


In the terminal, you can see the binder-specific error handler receiving an `ErrorMessage`.
```
2023-08-21T16:15:49.214+05:30  INFO 88595 --- [ool-65-thread-1] c.s.s.s.scs.ConsumerErrorHandlerDemo     : Received message on binding <functionTwo-in-0>: Hello world!
2023-08-21T16:15:49.215+05:30  WARN 88595 --- [ool-65-thread-1] c.s.s.c.s.b.i.JCSMPInboundChannelAdapter : Failed to consume a message from destination queueTwo - attempt 1
2023-08-21T16:15:50.217+05:30  INFO 88595 --- [ool-65-thread-1] c.s.s.s.scs.ConsumerErrorHandlerDemo     : Received message on binding <functionTwo-in-0>: Hello world!
2023-08-21T16:15:50.218+05:30  WARN 88595 --- [ool-65-thread-1] c.s.s.c.s.b.i.JCSMPInboundChannelAdapter : Failed to consume a message from destination queueTwo - attempt 2
2023-08-21T16:15:52.219+05:30  INFO 88595 --- [ool-65-thread-1] c.s.s.s.scs.ConsumerErrorHandlerDemo     : Received message on binding <functionTwo-in-0>: Hello world!
2023-08-21T16:15:52.219+05:30  WARN 88595 --- [ool-65-thread-1] c.s.s.c.s.b.i.JCSMPInboundChannelAdapter : Failed to consume a message from destination queueTwo - attempt 3
2023-08-21T16:15:52.224+05:30 ERROR 88595 --- [ool-65-thread-1] o.s.integration.handler.LoggingHandler   : org.springframework.messaging.MessageHandlingException: error occurred in message handler [org.springframework.cloud.stream.function.FunctionConfiguration$FunctionToDestinationBinder$1@7a3829bf], failedMessage=GenericMessage [payload=byte[12], headers={solace_senderTimestamp=1692614749185, solace_expiration=0, solace_destination=solace/function/two, solace_replicationGroupMessageId=rmid1:24c78-197d007757c-00000000-00001ff2, deliveryAttempt=3, solace_isReply=false, solace_timeToLive=0, solace_receiveTimestamp=0, acknowledgmentCallback=com.solace.spring.cloud.stream.binder.inbound.acknowledge.JCSMPAcknowledgementCallback@795cb7c, solace_discardIndication=false, solace_dmqEligible=true, solace_priority=-1, solace_redelivered=false, id=7300237c-09b0-5eeb-d76d-bf500af8c296, contentType=application/json, solace_senderId=Try-Me-Pub/solclientjs/chrome-116.0.0-OSX-10.15.7/0662633445/0001, timestamp=1692614749203}]
...
Caused by: java.lang.RuntimeException: Exception thrown
	at com.solace.samples.spring.scs.ConsumerErrorHandlerDemo.lambda$functionTwo$1(ConsumerErrorHandlerDemo.java:55)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.invokeConsumer(SimpleFunctionRegistry.java:990)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.doApply(SimpleFunctionRegistry.java:701)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.apply(SimpleFunctionRegistry.java:550)
	at org.springframework.cloud.stream.function.PartitionAwareFunctionWrapper.apply(PartitionAwareFunctionWrapper.java:88)
	at org.springframework.cloud.stream.function.FunctionConfiguration$FunctionWrapper.apply(FunctionConfiguration.java:785)
	at org.springframework.cloud.stream.function.FunctionConfiguration$FunctionToDestinationBinder$1.handleMessageInternal(FunctionConfiguration.java:621)
	at org.springframework.integration.handler.AbstractMessageHandler.doHandleMessage(AbstractMessageHandler.java:105)
	... 32 more

2023-08-21T16:15:52.235+05:30  INFO 88595 --- [ool-65-thread-1] c.s.s.s.scs.ConsumerErrorHandlerDemo     : Received error message on default error handler
2023-08-21T16:15:52.235+05:30  INFO 88595 --- [ool-65-thread-1] c.s.s.s.scs.ConsumerErrorHandlerDemo     : Original Message: org.springframework.messaging.MessageHandlingException: error occurred in message handler [org.springframework.cloud.stream.function.FunctionConfiguration$FunctionToDestinationBinder$1@7a3829bf], failedMessage=GenericMessage [payload=byte[12], headers={solace_senderTimestamp=1692614749185, solace_expiration=0, solace_destination=solace/function/two, solace_replicationGroupMessageId=rmid1:24c78-197d007757c-00000000-00001ff2, deliveryAttempt=3, solace_isReply=false, solace_timeToLive=0, solace_receiveTimestamp=0, acknowledgmentCallback=com.solace.spring.cloud.stream.binder.inbound.acknowledge.JCSMPAcknowledgementCallback@795cb7c, solace_discardIndication=false, solace_dmqEligible=true, solace_priority=-1, solace_redelivered=false, id=7300237c-09b0-5eeb-d76d-bf500af8c296, contentType=application/json, solace_senderId=Try-Me-Pub/solclientjs/chrome-116.0.0-OSX-10.15.7/0662633445/0001, timestamp=1692614749203}]
```