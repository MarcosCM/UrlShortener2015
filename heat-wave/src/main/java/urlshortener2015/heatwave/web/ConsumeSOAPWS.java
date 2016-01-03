package urlshortener2015.heatwave.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import urlshortener2015.heatwave.config.ProducerSOAPConfig;

import localhost._8081.soap.URLSTested;

@Service
public class ConsumeSOAPWS {

	/*
	 * Período de la tarea de actualizar las URLs
	 */
	private static final long T = 5*60; //5 minutos
	
	@Autowired
	private RedirectionTesterRequester redirectionTesterRequester;
	
	@Async
	@Scheduled(fixedRate=T*1000)
	public void consumeWS(){
		
		URLSTested response = redirectionTesterRequester.requestTestURLS();
	}
}
