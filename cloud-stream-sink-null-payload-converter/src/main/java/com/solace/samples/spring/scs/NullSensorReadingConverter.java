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
		if ((boolean) message.getHeaders().getOrDefault(SolaceBinderHeaders.NULL_PAYLOAD, false)) {
			return new SensorReading();
		} else {
			return super.convertFromInternal(message, targetClass, conversionHint);
		}
	}
}
