package urlshortener2015.heatwave.web;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import urlshortener2015.heatwave.entities.ShortURL;
import urlshortener2015.heatwave.repository.ShortURLRepository;

import localhost._8081.soap.TestYourURLS;
import localhost._8081.soap.URLSTested;

@Endpoint
public class RedirectionTesterWS {
	
	private static final Logger logger = LoggerFactory.getLogger(CustomConnectController.class);

	 /* Numero maximo de redirecciones*/
	 
	private static final int NUM_MAX_REDIRECCIONES = 5;
	
	private static final String TEST_URI = "http://localhost:8081/soap";

	@Autowired
	protected ShortURLRepository shortURLRepository;
	
	/**
	 * Se comprueba periodicamente que las Urls no tienen mas de
	 * 5 redirecciones.
	 */
	@PayloadRoot(namespace = TEST_URI, localPart = "TestYourURLS")
	@ResponsePayload
	public URLSTested testUrls(@RequestPayload TestYourURLS request){
		
		URLSTested respuesta = new URLSTested();
		
		HttpURLConnection.setFollowRedirects(false);
		HttpURLConnection conn = null;
		
		// Se obtienen las URLs de la base de datos
		List<ShortURL> URLS = shortURLRepository.findAll();
		
		for(ShortURL url : URLS){
			String urlTarget = url.getTarget();
			for(int i=0; i<5; i++){
				try{
					URL urlToConnect = new URL(urlTarget);
					
					conn = (HttpURLConnection) urlToConnect.openConnection();
					conn.setConnectTimeout(5000);
					conn.connect();
					
					// Si el código 
					if (conn.getResponseCode() / 100 == 4 || conn.getResponseCode() / 100 == 5){
						shortURLRepository.mark(url, false);
						conn.disconnect();
						break;
					}
					else if (conn.getResponseCode() / 100 == 3){
						// Si el codigo es un 3xx
						logger.info("redirection 300: " + urlTarget);
						//Alcanzado el limite de redirecciones.
						if(i == 4){
							shortURLRepository.mark(url, false);
							logger.info(url.getTarget() + " -> incorrecta");
							conn.disconnect();
							break;// Poner 404 en la base de datos
						}
						else{
							urlTarget = conn.getHeaderField("location").toString();
							conn.disconnect();
						}
					}
					// Si el codigo no es un 3xx no es redireccion
					else{
						// Si la URL estaba como no correcta en la base de datos se activa.
						shortURLRepository.mark(url, true);
						logger.info(url.getTarget() + " -> correcta");
						conn.disconnect();
						break;
					}
				}catch(Exception e){
					shortURLRepository.mark(url, false);
					conn.disconnect();
					break;// Poner 404 en la base de datos
				}
			}
		}
		
		return respuesta;
	}
}
