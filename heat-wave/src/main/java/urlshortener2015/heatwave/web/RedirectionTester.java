package urlshortener2015.heatwave.web;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class RedirectionTester {
	
	/*
	 * Número máximo de redirecciones
	 */
	private static final int NUM_MAX_REDIRECCIONES = 5;
	
	/**
	 * Se comprueba periódicamente que las Urls no tienen más de
	 * 5 redirecciones.
	 */
	@Async
	public void testUrls (){
		
		Client client = ClientBuilder.newClient();
		Response response;
		
		// Se obtienen las URLs de la base de datos
		String [] URLS = null; // Falta método para obtener todas las urls de la base de datos.
		
		for(String url : URLS){
			for(int i=0; i<=NUM_MAX_REDIRECCIONES; i++){
				response = client.target(url).request().get();
				// Si el código es un 3xx
				if (response.getStatus() % 100 == 3){
					//Alcanzado el límite de redirecciones.
					if(i == NUM_MAX_REDIRECCIONES){

						break;// Poner 404 en la base de datos
					}
					else{
						url = response.getLocation().toString();
					}
				}
				// Si el código no es un 3xx no es redirección
				else{
					// Si la URL estaba como no correcta en la base de datos se activa.
					
					break;
				}
			}
		}
		 
	}
}
