package urlshortener2015.heatwave.web;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import urlshortener2015.heatwave.entities.ShortURL;
import urlshortener2015.heatwave.repository.ShortURLRepository;

import localhost._8081.soap.TestYourURLS;
import localhost._8081.soap.URLSTested;

@Service
public class RedirectionTesterRequester {
	
	private static final Logger logger = LoggerFactory.getLogger(RedirectionTesterRequester.class);

	/*
	 * Período de la tarea de actualizar las URLs
	 */
	private static final long T = 30; //5 minutos
	
	@Autowired
	protected ShortURLRepository shortURLRepository;
	
	/**
	 * Periodically checks all URLs safeness
	 */
	@Async
	@Scheduled(fixedRate=T*1000)
	public void requestTestURLS() throws Exception{
		
		/*TestYourURLS request = new TestYourURLS();
		logger.info("Requesting Test for URLS (SOAP service)");
		
		URLSTested response = (URLSTested) getWebServiceTemplate().marshalSendAndReceive(
				"http://localhost:8081/ws/URLTester.wsdl",
				request,
				new SoapActionCallback("http://localhost:8081/URLSTested"));
		

		if(response != null) logger.info("URLs Tested!");
		else logger.info("SOAP failed! in testing URLs");*/
		
		
	}
}
