package urlshortener2015.heatwave.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import java.util.Date;
import java.util.ArrayList;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import javax.servlet.http.HttpServletRequest;

import org.springframework.messaging.simp.SimpMessagingTemplate;
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
import urlshortener2015.heatwave.entities.noun;
import urlshortener2015.heatwave.entities.Estadisticas;
import urlshortener2015.heatwave.entities.HelloMessage;
import urlshortener2015.heatwave.entities.ShortURL;
import urlshortener2015.heatwave.entities.Sugerencia;
import urlshortener2015.heatwave.exceptions.Error400Response;
import urlshortener2015.heatwave.repository.ClickRepository;
import urlshortener2015.heatwave.repository.ShortURLRepository;
import urlshortener2015.heatwave.utils.HttpServletRequestUtils;
import urlshortener2015.heatwave.utils.Sugerencias;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@RestController
public class UrlShortenerControllerWithLogs {

	private static final Logger logger = LoggerFactory.getLogger(UrlShortenerControllerWithLogs.class);

	@Autowired
	private ShortURLRepository shortURLRepository;
	@Autowired
	private SimpMessagingTemplate template;
	@Autowired
	private ClickRepository clickRepository;

	private void createAndSaveClick(String hash, String browser, String platform, String ip) {
		Click cl = new Click(null, hash, new Date(System.currentTimeMillis()), browser, platform, ip, null);
		cl = clickRepository.insert(cl);
		logger.info(cl != null ? "[" + hash + "] saved with id [" + cl.getId() + "]" : "[" + hash + "] was not saved");
	}

	private ShortURL createAndSaveIfValid(String url, String customTag) {
		UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" });
		if (urlValidator.isValid(url)) {
			String id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
			if (customTag != null && !customTag.equals("")) {
				id = customTag;
			}

			// Se hace un get de la url a acortar para comprobar que la url no
			// es una redirección a sí misma.
			Client client = ClientBuilder.newClient();
			Response response = client.target(url).request().get();
			// Si el código es un 3xx y el Location es 'url' --> es redirección
			// de sí misma.
			if (response.getStatus() / 100 == 3) {
				try {
					URI entrada = new URI(url);
					if (entrada.compareTo(response.getLocation()) == 0)
						throw new Error400Response("La URL a acortar es redirección de sí misma.");
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}

			// si ya existe devoler null
			ShortURL su = new ShortURL(id, url,
					linkTo(methodOn(UrlShortenerControllerWithLogs.class).redirectTo(id, null)).toUri(),
					new Date(System.currentTimeMillis()), HttpStatus.TEMPORARY_REDIRECT.value(), true);
			return shortURLRepository.insert(su);
		} else {
			return null;
		}
	}

	private ResponseEntity<?> createSuccessfulRedirectToResponse(ShortURL l) {
		HttpHeaders h = new HttpHeaders();
		h.setLocation(URI.create(l.getTarget()));
		return new ResponseEntity<>(h, HttpStatus.valueOf(l.getMode()));
	}

	private ResponseEntity<?> createSuccessfulRedirectToStadistic(ShortURL l) {
		// En l tienes todos los datos de la shortURL
		String resultado = "Número de clicks: " + clickRepository.countByHash(l.getHash());
		resultado += "</br>Url: " + l.getTarget();
		resultado += "</br>Fecha: " + l.getDate().toString();
		String a = "<!DOCTYPE html>\n" + "<html>\n" + "<head>\n" + "    <title>Hello WebSocket</title>\n"
				+ "    <script src='js/sockjs-0.3.4.js'></script>\n" + "    <script src='js/stomp.js'></script>\n"
				+ "    <script src='js/webSocketImpl.js'></script>\n" + "</head>\n" + "<body onload='connect()'>\n"
				+ "<noscript><h2 style='color: #ff0000'>Seems your browser doesn't support Javascript! Websocket relies on Javascript being enabled. Please enable\n"
				+ "    Javascript and reload this page!</h2></noscript>\n" + "<div>\n" + "    <div>\n" + "    </div>\n"
				+ "    <div id='conversationDiv'>\n" + "<p id='response'>" + resultado + "</p>\n" + "    </div>\n"
				+ "</div>\n" + "</body>\n" + "</html>";
		return new ResponseEntity<>(a, HttpStatus.OK);
	}

	private ResponseEntity<?> createSuccessfulRedirectToStadisticJson(ShortURL l) {
		// En l tienes todos los datos de la shortURL
		Estadisticas stats = new Estadisticas(clickRepository.countByHash(l.getHash()), l.getTarget(),
				l.getDate().toString());
		String Json = stats.toString();
		return new ResponseEntity<>(Json, HttpStatus.OK);
	}

	@RequestMapping(value = "/{id:(?!link|!stadistics|index).*}", method = RequestMethod.GET)
	public ResponseEntity<?> redirectTo(@PathVariable String id, HttpServletRequest request) {
		logger.info("Requested redirection with hash " + id);
		ShortURL l = shortURLRepository.findByHash(id);
		if (l != null) {
			createAndSaveClick(id, HttpServletRequestUtils.getBrowser(request),
					HttpServletRequestUtils.getPlatform(request), HttpServletRequestUtils.getRemoteAddr(request));
			Estadisticas stats = new Estadisticas(clickRepository.countByHash(l.getHash()), l.getTarget(),
					l.getDate().toString());
			// this.template.convertAndSend("/sockets/"+id, new
			// Greeting(resultado));
			this.template.convertAndSend("/sockets/" + id, stats);
			return createSuccessfulRedirectToResponse(l);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "/sugerencias/recomendadas", method = RequestMethod.GET)
	public ResponseEntity<ArrayList<Sugerencia>> sugerencias(@RequestParam(value = "url", required = false) String url,
			@RequestParam(value = "personalizada", required = false) String personalizada) {
		ArrayList<Sugerencia> lista = new ArrayList<Sugerencia>();
		if (personalizada != null && !personalizada.equals("")
				&& shortURLRepository.findByHash(personalizada) != null) {
			String SugerenciaSufijo = Sugerencias.sugerenciaSufijos(shortURLRepository, personalizada);
			String SugerenciaSufijo2 = SugerenciaSufijo;

			while (SugerenciaSufijo2.equals(SugerenciaSufijo)) {
				SugerenciaSufijo2 = Sugerencias.sugerenciaSufijos(shortURLRepository, personalizada);
			}

			lista.add(new Sugerencia(SugerenciaSufijo2));
			lista.add(new Sugerencia(SugerenciaSufijo));
			RestTemplate restTemplate = new RestTemplate();
			try {
				ResponseEntity<String> response = restTemplate
						.getForEntity("http://words.bighugelabs.com/api/2/c302f07e3593264f58a7366800330462/"
								+ personalizada + "/json", String.class);
				String body = response.getBody();
				// a partir de la posicion 5 están los resultados
				// se añaden dos sugerencias si la api ha devuelto resultados
				// son en posiciones impares (en las pares hay comas
				lista.add(new Sugerencia(body.split("\"")[5]));
				lista.add(new Sugerencia(body.split("\"")[7]));
				lista.add(new Sugerencia(body.split("\"")[9]));

			} catch (Exception a) {
			}
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	/*
	 * @RequestMapping(value = "/{id:(?!link|index).*}+") public String
	 * redirectToEstadisticas(@PathVariable String id, HttpServletRequest
	 * request) { logger.info("Requested redirection with hash " + id); return
	 * "static/estadisticas.html"; }
	 */

	@RequestMapping(value = "/{id:(?!link|!stadistics|index).*}+", method = RequestMethod.GET)
	public ResponseEntity<?> redirectToEstadisticas(@PathVariable String id, HttpServletRequest request) {
		logger.info("Requested redirection with hash " + id);
		ShortURL l = shortURLRepository.findByHash(id);
		if (l != null) {
			return createSuccessfulRedirectToStadistic(l);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "/{id:(?!link|!stadistics|index).*}+/json", method = RequestMethod.GET)
	public ResponseEntity<?> redirectToEstadisticasJson(@PathVariable String id, HttpServletRequest request) {
		logger.info("Requested redirection with hash " + id);
		ShortURL l = shortURLRepository.findByHash(id);
		if (l != null) {
			return createSuccessfulRedirectToStadisticJson(l);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "/link", method = RequestMethod.POST)
	public ResponseEntity<ShortURL> shortener(@RequestParam("url") String url,
			@RequestParam(value = "personalizada", required = false) String personalizada,
			@RequestParam(value = "sponsor", required = false) String sponsor,
			@RequestParam(value = "brand", required = false) String brand, HttpServletRequest request) {
		logger.info("Requested new short for uri " + url);
		if (personalizada != null && !personalizada.equals("")) {
			ShortURL urlconID = shortURLRepository.findByHash(personalizada);
			if (urlconID != null) {
				// la url personalizada ya existe
				String SugerenciaSufijo = Sugerencias.sugerenciaSufijos(shortURLRepository, personalizada);
				String SugerenciaSufijo2 = SugerenciaSufijo;
				while (SugerenciaSufijo2.equals(SugerenciaSufijo)) {
					SugerenciaSufijo2 = Sugerencias.sugerenciaSufijos(shortURLRepository, personalizada);
				}
				// las recomendaciones se separan con el separador ":"
				throw new Error400Response(
						"La URL a personalizar ya existe:" + SugerenciaSufijo + ":" + SugerenciaSufijo2);

				// return new ResponseEntity<>(urlconID,
				// HttpStatus.BAD_REQUEST);
			}
		}
		ShortURL su = createAndSaveIfValid(url, personalizada);
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
