package com.solace.samples.spring.boot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

@Configuration
@ConditionalOnProperty(prefix = "sample.solace", name = "jndi-name")
public class SolaceJndiConfiguration {

	@Bean
	@Primary
	public JndiObjectFactoryBean jndiConnectionFactory(JndiTemplate jndiTemplate,
													   @Value("${sample.solace.jndi-name}") String jndiName) {
		JndiObjectFactoryBean factoryBean = new JndiObjectFactoryBean();
		factoryBean.setJndiTemplate(jndiTemplate);
		factoryBean.setJndiName(jndiName);
		return factoryBean;
	}

	@Bean
	public JndiDestinationResolver jndiDestinationResolver(JndiTemplate jndiTemplate) {
		JndiDestinationResolver jdr = new JndiDestinationResolver();
		jdr.setJndiTemplate(jndiTemplate);
		return jdr;
	}
}
