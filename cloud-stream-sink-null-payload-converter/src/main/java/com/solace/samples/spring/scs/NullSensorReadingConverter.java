package com.solace.samples.spring.scs;

import org.springframework.messaging.Message;
import org.springframework.messaging.converter.AbstractMessageConverter;

import com.solace.samples.spring.common.SensorReading;
import com.solace.spring.cloud.stream.binder.messaging.SolaceBinderHeaders;

public class NullSensorReadingConverter extends AbstractMessageConverter {
	@Override
	protected boolean supports(Class<?> clazz) {
		return SensorReading.class.equals(clazz);
	}

	@Override
	protected Object convertFromInternal(Message<?> message, Class<?> targetClass, Object conversionHint) {
		/*
		 * NOTE: If the payload is null, then the "solace_scst_nullPayload" message header will be set to true. 
		 * Because the Spring framework does not allow for a message to have a null payload we need to 
		 * add a custom message converter to handle that scenario to return a message with either a 
		 * default or custom payload. 
		 */
		
		if ((boolean) message.getHeaders().getOrDefault(SolaceBinderHeaders.NULL_PAYLOAD, false)) {
			return new SensorReading();
		} else {
			return super.convertFromInternal(message, targetClass, conversionHint);
		}
	}
	
	/*
	 * How to test this?
	 * 
	 * Spring doesn't allow sending messages with NULL payload. Hence, a unit test can not be 
	 * successfully built or run. 
	 * 
	 * However to test this feature, we can follow any of the following options manually:
	 * 
	 * 1. Use of Try Me! tool in the PubSub+ Manager tool
	 * 		Publish events on the topic with empty message content
	 * 2. Use sdkperf to publish events on the topic with emptry payload
	 * 		./sdkperf_java.sh -cip="localhost:55555" -ptl "sensor/temperature/99" -mn 1 -mr 1 -md
	 */

}
