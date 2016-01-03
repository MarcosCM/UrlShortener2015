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
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import urlshortener2015.heatwave.entities.ShortURL;
import urlshortener2015.heatwave.repository.ShortURLRepository;

import localhost._8081.soap.TestYourURLS;
import localhost._8081.soap.URLSTested;

public class RedirectionTesterRequester extends WebServiceGatewaySupport{
	
	private static final Logger logger = LoggerFactory.getLogger(RedirectionTesterRequester.class);
	
	/**
	 * Periodically checks all URLs safeness
	 */
	public URLSTested requestTestURLS(){
		TestYourURLS request = new TestYourURLS();
		URLSTested response = null;
		
		try{
		logger.info("Requesting Test for URLS (SOAP service)");
		
		response = (URLSTested) getWebServiceTemplate().marshalSendAndReceive(
				"http://localhost:8081/ws",
				request);
		
		if(response != null) logger.info("URLs Tested!");
		else logger.info("SOAP failed! in testing URLs");
		}catch(Exception e){
			logger.info("SOAP failed! in testing URLs:\n" + e.getMessage());
		}
		return response;
	}
}
