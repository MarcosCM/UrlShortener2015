package urlshortener2015.heatwave.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import urlshortener2015.heatwave.web.RedirectionTesterRequester;

@Configuration
public class ConsumerSOAPConfig {

	@Bean
	public Jaxb2Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setContextPath("urlshortener2015.heatwave.soap");
		return marshaller;
	}

	@Bean
	public RedirectionTesterRequester redirectionTesterRequester(Jaxb2Marshaller marshaller) {
		RedirectionTesterRequester client = new RedirectionTesterRequester();
		client.setDefaultUri("http://localhost:8081/soap");
		client.setMarshaller(marshaller);
		client.setUnmarshaller(marshaller);
		return client;
	}
}
