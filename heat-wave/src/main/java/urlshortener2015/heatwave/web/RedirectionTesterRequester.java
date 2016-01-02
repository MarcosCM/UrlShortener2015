package urlshortener2015.heatwave.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import urlshortener2015.heatwave.entities.ShortURL;
import urlshortener2015.heatwave.repository.ShortURLRepository;

import io.spring.guides.gs_producing_web_service.TestYourURLS;
import io.spring.guides.gs_producing_web_service.URLSTested;

@Service
public class RedirectionTesterRequester extends WebServiceGatewaySupport {
	
	private static final Logger logger = LoggerFactory.getLogger(UrlShortenerControllerWithLogs.class);

	/*
	 * Período de la tarea de actualizar las URLs
	 */
	private static final long T = 5*60; //5 minutos
	
	@Autowired
	protected ShortURLRepository shortURLRepository;
	
	/**
	 * Se comprueba periódicamente que las Urls no tienen más de
	 * 5 redirecciones.
	 */
	@Async
	@Scheduled(fixedRate=T*1000)
	public void requestTestURLS(){
		
		TestYourURLS request = new TestYourURLS();
		logger.info("Requesting Test for URLS (SOAP service)");
		
		URLSTested response = (URLSTested) getWebServiceTemplate().marshalSendAndReceive(
				"http://localhost:8081/TestYourURLS",
				request,
				new SoapActionCallback("http://ws.cdyne.com/WeatherWS/URLSTested"));
		
		if(response != null) logger.info("URLs Tested!");
		else logger.info("SOAP failed! in testing URLs");
	}
}

