package urlshortener2015.heatwave.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import urlshortener2015.heatwave.soap.TestYourURLS;
import urlshortener2015.heatwave.soap.URLSTested;

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
