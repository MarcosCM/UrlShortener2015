package urlshortener2015.heatwave.web;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import urlshortener2015.common.domain.ShortURL;
import urlshortener2015.common.repository.ShortURLRepository;

@Service
public class RedirectionTester {
	
	/*
	 * Número máximo de redirecciones
	 */
	private static final int NUM_MAX_REDIRECCIONES = 5;
	
	@Autowired
	protected ShortURLRepository shortURLRepository;
	
	/**
	 * Se comprueba periódicamente que las Urls no tienen más de
	 * 5 redirecciones.
	 */
	@Async
	public void testUrls (){
		
		Client client = ClientBuilder.newClient();
		Response response;
		
		// Se obtienen las URLs de la base de datos
		List<ShortURL> URLS = shortURLRepository.list((long) 0, (long) 0); //Falta saber qué son limit y offset
		
		for(ShortURL url : URLS){
			String urlTarget = url.getTarget();
			for(int i=0; i<=NUM_MAX_REDIRECCIONES; i++){
				response = client.target(urlTarget).request().get();
				// Si el código es un 3xx
				if (response.getStatus() / 100 == 3){
					//Alcanzado el límite de redirecciones.
					if(i == NUM_MAX_REDIRECCIONES){
						shortURLRepository.mark(url, false);
						break;// Poner 404 en la base de datos
					}
					else{
						urlTarget = response.getLocation().toString();
					}
				}
				// Si el código no es un 3xx no es redirección
				else{
					// Si la URL estaba como no correcta en la base de datos se activa.
					shortURLRepository.mark(url, true);
					break;
				}
			}
		}
		 
	}
}
