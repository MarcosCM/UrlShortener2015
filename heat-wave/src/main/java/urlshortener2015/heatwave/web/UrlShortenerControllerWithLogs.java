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

import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.hash.Hashing;

import urlshortener2015.heatwave.entities.Click;
import urlshortener2015.heatwave.entities.ShortURL;
import urlshortener2015.heatwave.entities.Sugerencia;
import urlshortener2015.heatwave.exceptions.Error400Response;
import urlshortener2015.heatwave.repository.ClickRepository;
import urlshortener2015.heatwave.repository.ShortURLRepository;
import urlshortener2015.heatwave.utils.HttpServletRequestUtils;
import urlshortener2015.heatwave.utils.Sugerencias;

@RestController
public class UrlShortenerControllerWithLogs {

	private static final Logger logger = LoggerFactory.getLogger(UrlShortenerControllerWithLogs.class);

	@Autowired
	private ShortURLRepository shortURLRepository;

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
			
			// Se hace un get de la url a acortar para comprobar que la url no es una redirección a sí misma.
			Client client = ClientBuilder.newClient();
			Response response = client.target(url).request().get();
			// Si el código es un 3xx y el Location es 'url' --> es redirección de sí misma.
			if (response.getStatus() / 100 == 3){
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
			return shortURLRepository.save(su);
		}
		else {
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
		String resultado = "Este enlace ha recibido " + clickRepository.countByHash(l.getHash()) + " clicks";
		resultado += "</br>La url es " + l.getTarget();
		resultado += "</br>La fecha de creación es " + l.getDate().toString();
		return new ResponseEntity<>(resultado, HttpStatus.OK);
	}

	@RequestMapping(value = "/{id:(?!link|index).*}", method = RequestMethod.GET)
	public ResponseEntity<?> redirectTo(@PathVariable String id, HttpServletRequest request) {
		logger.info("Requested redirection with hash " + id);
		ShortURL l = shortURLRepository.findByHash(id);
		if (l != null) {
			createAndSaveClick(id, HttpServletRequestUtils.getBrowser(request),
					HttpServletRequestUtils.getPlatform(request), HttpServletRequestUtils.getRemoteAddr(request));
			return createSuccessfulRedirectToResponse(l);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "/sugerencias/recomendadas", method = RequestMethod.GET)
	public ResponseEntity<ArrayList<Sugerencia>> sugerencias(@RequestParam(value = "url", required = false) String url,
			@RequestParam(value = "personalizada", required = false) String personalizada) {
		ArrayList<Sugerencia> lista = new ArrayList<Sugerencia>();
		if (personalizada != null && !personalizada.equals("") &&
				shortURLRepository.findByHash(personalizada)!=null) {
			String SugerenciaSufijo = Sugerencias.sugerenciaSufijos(shortURLRepository, personalizada);
			String SugerenciaSufijo2 = SugerenciaSufijo;
			
			while (SugerenciaSufijo2.equals(SugerenciaSufijo)) {
				SugerenciaSufijo2 = Sugerencias.sugerenciaSufijos(shortURLRepository, personalizada);
			}
			lista.add(new Sugerencia(SugerenciaSufijo2));
			lista.add(new Sugerencia(SugerenciaSufijo));
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	@RequestMapping(value = "/{id:(?!link|index).*}+", method = RequestMethod.GET)
	public ResponseEntity<?> redirectToEstadisticas(@PathVariable String id, HttpServletRequest request) {
		logger.info("Requested redirection with hash " + id);
		ShortURL l = shortURLRepository.findByHash(id);
		if (l != null) {
			createAndSaveClick(id, HttpServletRequestUtils.getBrowser(request),
					HttpServletRequestUtils.getPlatform(request), HttpServletRequestUtils.getRemoteAddr(request));
			return createSuccessfulRedirectToStadistic(l);
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
				throw new Error400Response("La URL a personalizar ya existe:" + SugerenciaSufijo + ":" + SugerenciaSufijo2);

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
}
