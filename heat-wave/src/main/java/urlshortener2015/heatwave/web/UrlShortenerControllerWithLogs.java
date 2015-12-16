package urlshortener2015.heatwave.web;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import java.util.Date;
import java.util.ArrayList;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.common.hash.Hashing;

import urlshortener2015.heatwave.entities.Click;
import urlshortener2015.heatwave.entities.Estadisticas;
import urlshortener2015.heatwave.entities.HelloMessage;
import urlshortener2015.heatwave.entities.ShortURL;
import urlshortener2015.heatwave.entities.Sugerencia;
import urlshortener2015.heatwave.exceptions.Error400Response;
import urlshortener2015.heatwave.repository.ClickRepository;
import urlshortener2015.heatwave.repository.ShortURLRepository;
import urlshortener2015.heatwave.utils.Sugerencias;
import org.springframework.messaging.handler.annotation.MessageMapping;

@RestController
public class UrlShortenerControllerWithLogs {

	private static final Logger logger = LoggerFactory.getLogger(UrlShortenerControllerWithLogs.class);

	@Autowired
	private ShortURLRepository shortURLRepository;
	
	@Autowired
	private ClickRepository clickRepository;

	/**
	 * Guarda un click hecho sobre una URL acortada
	 * @param hash Identificador de la URL (hash o etiqueta)
	 * @param browser Navegador desde el que se ha hecho click
	 * @param platform Sistema Operativo/Plataforma desde la que se ha hecho click
	 * @param ip IP desde la que se ha hecho click
	 */
	public static void createAndSaveClick(String hash, String browser, String platform, String ip, ClickRepository clickRepository) {
		Click cl = new Click(null, hash, new Date(System.currentTimeMillis()), browser, platform, ip, null);
		cl = clickRepository.insert(cl);
		logger.info(cl != null ? "[" + hash + "] saved with id [" + cl.getId() + "]" : "[" + hash + "] was not saved");
	}

	/**
	 * Crea una URL acortada
	 * @param url URL a acortar
	 * @param customTag Etiqueta personalizada
	 * @param ads Mostrar anuncios
	 * @return URL acortada en caso de exito, error en caso contrario
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public static ShortURL createAndSaveIfValid(String url, String customTag, Boolean ads, ShortURLRepository shortURLRepository) throws MalformedURLException, URISyntaxException {
		UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" });
		if (urlValidator.isValid(url)) {
			String id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
			if (customTag != null && !customTag.equals("")) {
				id = customTag;
			}

			// Se hace un get de la url a acortar para comprobar que la url no es una redireccion a si misma.
			Client client = ClientBuilder.newClient();
			Response response = client.target(url).request().get();
			// Si el codigo es un 3xx y el Location es 'url' --> es redireccion de si misma.
			if (response.getStatus() / 100 == 3){
				try {
					URI entrada = new URI(url);
					if (entrada.compareTo(response.getLocation()) == 0)
						throw new Error400Response("La URL a acortar es redireccion de si misma.");
				} catch (URISyntaxException e) {
					throw new Error400Response("La URL a acortar no es valida.");
				}
			}
			
			// Si ya existe devolver null
			ShortURL su = new ShortURL(id, url, new URI(id), new Date(System.currentTimeMillis()),
					HttpStatus.TEMPORARY_REDIRECT.value(), true, ads);
			return shortURLRepository.insert(su);

		}
		else {
			return null;
		}
	}
	
	/**
	 * Redirecciona a la pagina de estadisticas
	 * @param url URL sobre la que generar las estadisticas
	 * @return Pagina de estadisticas
	 */
	public static ResponseEntity<?> createSuccessfulRedirectToStatistic(ShortURL url, ClickRepository clickRepository) {
		// En url tienes todos los datos de la shortURL
		String resultado = "Número de clicks: " + clickRepository.countByHash(url.getHash());
		resultado += "</br>Url: " + url.getTarget();
		resultado += "</br>Fecha: " + url.getDate().toString();
		String a = "<!DOCTYPE html>\n" + "<html>\n" + "<head>\n" + "    <title>Hello WebSocket</title>\n"
				+ "    <script src='js/sockjs-0.3.4.js'></script>\n" + "    <script src='js/stomp.js'></script>\n"
				+ "    <script src='js/webSocketImpl.js'></script>\n" + "</head>\n" + "<body onload='connect()'>\n"
				+ "<noscript><h2 style='color: #ff0000'>Seems your browser doesn't support Javascript! Websocket relies on Javascript being enabled. Please enable\n"
				+ "    Javascript and reload this page!</h2></noscript>\n" + "<div>\n" + "    <div>\n" + "    </div>\n"
				+ "    <div id='conversationDiv'>\n" + "<p id='response'>" + resultado + "</p>\n" + "    </div>\n"
				+ "</div>\n" + "</body>\n" + "</html>";
		return new ResponseEntity<>(a, HttpStatus.OK);
	}

	public static ResponseEntity<?> createSuccessfulRedirectToStatisticJson(ShortURL url, ClickRepository clickRepository) {
		// En l tienes todos los datos de la shortURL
		Estadisticas stats = new Estadisticas(clickRepository.countByHash(url.getHash()), url.getTarget(),
				url.getDate().toString());
		//String json = stats.toString();
		return new ResponseEntity<>(stats, HttpStatus.OK);
	}
	
	private static ArrayList<Sugerencia> listaSugerencias(String customTag, ShortURLRepository shortURLRepository){
		ArrayList<Sugerencia> lista = new ArrayList<Sugerencia>();
		if (customTag != null && !customTag.equals("") && shortURLRepository.findByHash(customTag) != null) {
			String sugerenciaSufijo = Sugerencias.sugerenciaSufijos(shortURLRepository, customTag);
			String sugerenciaSufijo2 = sugerenciaSufijo;

			while (sugerenciaSufijo2.equals(sugerenciaSufijo)) {
				sugerenciaSufijo2 = Sugerencias.sugerenciaSufijos(shortURLRepository, customTag);
			}

			lista.add(new Sugerencia(sugerenciaSufijo2));
			lista.add(new Sugerencia(sugerenciaSufijo));
			RestTemplate restTemplate = new RestTemplate();
			try {
				ResponseEntity<String> response = restTemplate.getForEntity("http://words.bighugelabs.com/api/2/c302f07e3593264f58a7366800330462/"
								+ customTag + "/json", String.class);
				String body = response.getBody();
				// a partir de la posicion 5 estan los resultados
				// se anaden dos sugerencias si la api ha devuelto resultados
				// son en posiciones impares (en las pares hay comas)
				int sugerenciasIngles=0;
				int i=5;
				while(sugerenciasIngles<4){
					if(shortURLRepository.findByHash(body.split("\"")[i])==null){
						lista.add(new Sugerencia(body.split("\"")[i]));
						sugerenciasIngles++;
					}
					// son en posiciones impares (en las pares hay comas)
					i+=2;
				}
			} catch (Exception a) {}
		}
		return lista;
	}
	
	/**
	 * Devuelve sugerencias para una URL personalizada que ya existe
	 * @param url URL sobre la que se esta escribiendo una etiqueta
	 * @param customTag Etiqueta para dicha URL
	 * @return Lista de sugerencias o vacio si la etiqueta no esta cogida
	 */
	@RequestMapping(value = "/sugerencias/recomendadas", method = RequestMethod.GET)
	public ResponseEntity<ArrayList<Sugerencia>> sugerencias(@RequestParam(value = "url", required = false) String url,
			@RequestParam(value = "customTag", required = false) String customTag) {
		logger.info("Getting suggestions for the custom tag: " + customTag);
		ArrayList<Sugerencia> lista = UrlShortenerControllerWithLogs.listaSugerencias(customTag, shortURLRepository);
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	/**
	 * Redirige a la pagina de estadisticas
	 * @param id Hash o etiqueta de la URL
	 * @param request Peticion
	 * @return Pagina de estadisticas
	 */
	@RequestMapping(value = "/{id:(?!link|!stadistics|index).*}+", method = RequestMethod.GET)
	public ResponseEntity<?> redirectToEstadisticas(@PathVariable String id, HttpServletRequest request) {
		logger.info("Requested redirection with hash " + id);
		ShortURL l = shortURLRepository.findByHash(id);
		if (l != null) {
			return UrlShortenerControllerWithLogs.createSuccessfulRedirectToStatistic(l, clickRepository);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "/{id:(?!link|!stadistics|index).*}+/json", method = RequestMethod.GET)
	public ResponseEntity<?> redirectToEstadisticasJson(@PathVariable String id, HttpServletRequest request) {
		logger.info("Requested redirection with hash " + id);
		ShortURL l = shortURLRepository.findByHash(id);
		if (l != null) {
			return UrlShortenerControllerWithLogs.createSuccessfulRedirectToStatisticJson(l, clickRepository);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Acorta una URL especificada
	 * @param url URL a acortar
	 * @param customTag Etiqueta personalizada solicitada para la URL
	 * @param request Peticion
	 * @return Mensaje de exito o error
	 * @throws URISyntaxException 
	 * @throws MalformedURLException 
	 */
	@RequestMapping(value = "/link", method = RequestMethod.POST)
	public ResponseEntity<ShortURL> shortener(@RequestParam("url") String url,
			@RequestParam(value = "customTag", required = false) String customTag,
			@RequestParam(value = "disableAd", required = false) Boolean disableAd,
			HttpServletRequest request) throws MalformedURLException, URISyntaxException {
		logger.info("Requested new short for uri " + url);
		Boolean ads;
		if (disableAd == null || !disableAd) ads = new Boolean(true);
		else ads = new Boolean(false);
		if (customTag != null && !customTag.equals("")) {
			ShortURL urlConID = shortURLRepository.findByHash(customTag);
			if (urlConID != null) {
				// la url personalizada ya existe
				String SugerenciaSufijo = Sugerencias.sugerenciaSufijos(shortURLRepository, customTag);
				String SugerenciaSufijo2 = SugerenciaSufijo;
				while (SugerenciaSufijo2.equals(SugerenciaSufijo)) {
					SugerenciaSufijo2 = Sugerencias.sugerenciaSufijos(shortURLRepository, customTag);
				}
				// las recomendaciones se separan con el separador ":"
				ArrayList<Sugerencia> lista = UrlShortenerControllerWithLogs.listaSugerencias(customTag, shortURLRepository);
				String messageError = "La URL a personalizar ya existe";
				for (int i=0; i<lista.size(); i++){
					messageError += ":" + lista.get(i).getRecomendacion();
				}
				throw new Error400Response(messageError);

				// return new ResponseEntity<>(urlconID,
				// HttpStatus.BAD_REQUEST);
			}
		}
		ShortURL su = UrlShortenerControllerWithLogs.createAndSaveIfValid(url, customTag, ads, shortURLRepository);
		if (su != null) {
			HttpHeaders h = new HttpHeaders();
			h.setLocation(su.getUri());
			return new ResponseEntity<>(su, h, HttpStatus.CREATED);
		} else {
			throw new Error400Response("La URL a acortar no es válida");
		}
	}

	// A donde llega los mensajes de los sockets desde el cliente
	@MessageMapping("/stadistics")
	// @SendTo("/sockets/IDPARAMANDAR")
	public Estadisticas respuestaSocket(HelloMessage infoQueLlega) throws Exception {
		return new Estadisticas(new Long(1), "", "");
	}

}
