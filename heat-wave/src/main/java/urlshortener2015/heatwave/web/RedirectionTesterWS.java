package urlshortener2015.heatwave.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import urlshortener2015.heatwave.entities.ShortURL;
import urlshortener2015.heatwave.repository.ShortURLRepository;

import urlshortener2015.heatwave.soap.TestYourURLS;
import urlshortener2015.heatwave.soap.URLSTested;

@Endpoint
public class RedirectionTesterWS {
	
	private static final Logger logger = LoggerFactory.getLogger(RedirectionTesterWS.class);

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
		respuesta.setNumURLs(0);
		
		HttpURLConnection.setFollowRedirects(false);
		HttpURLConnection conn = null;
		
		// Se obtienen las URLs de la base de datos
		List<ShortURL> URLS = shortURLRepository.findAll();
		
		for(ShortURL url : URLS){
			String urlTarget = url.getTarget();
			for(int i=0; i<NUM_MAX_REDIRECCIONES; i++){
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
						//Alcanzado el limite de redirecciones.
						if(i == NUM_MAX_REDIRECCIONES-1){
							shortURLRepository.mark(url, false);
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
						if(executeRules(url)){
							shortURLRepository.mark(url, true);
						}
						else{
							shortURLRepository.mark(url, false);
						}
						conn.disconnect();
						break;
					}
				}catch(Exception e){
					logger.debug("Exception: " + e.getMessage());
					shortURLRepository.mark(url, false);
					conn.disconnect();
					break;// Poner 404 en la base de datos
				}
			}
			
			respuesta.setNumURLs(respuesta.getNumURLs() + 1);
		}
		
		return respuesta;
	}
	
	private boolean executeRules(ShortURL url){
		boolean res = true;
		
		if(url.getRules() == null || url.getRules().isEmpty()) return true;
		Collection<String> rules = url.getRules().values();
		String code;
		/*
		 * Se obtienen todos los scripts y se comprueban uno a uno
		 * que son correctos.
		 */
		for (String rule : rules){
			code = "checkURL(){\n"
					+ rule + "\n"
					+ "}\n\n"
					+ "checkURL " + url.getTarget();
			Process p;
			try{
				String [] cmd = {"bash", "-c", code};
				p = Runtime.getRuntime().exec(cmd);
			    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			    String s = br.readLine();
			    // Un script es correcto si y solo si devuelve "true".
			    res &= Boolean.parseBoolean(s);
			}catch (Exception e){
				logger.debug("Fallan reglas de URL: " + url.getTarget());
				return false;
			}
		}
		
		return res;
	}
}
