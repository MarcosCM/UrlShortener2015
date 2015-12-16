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
	
	/**
	 * Guarda un click hecho sobre una URL acortada
	 * @param hash Identificador de la URL (hash o etiqueta)
	 * @param browser Navegador desde el que se ha hecho click
	 * @param platform Sistema Operativo/Plataforma desde la que se ha hecho click
	 * @param ip IP desde la que se ha hecho click
	 */
	private void createAndSaveClick(String hash, String browser, String platform, String ip) {
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
	private ShortURL createAndSaveIfValid(String url, String customTag, Boolean ads) throws MalformedURLException, URISyntaxException {
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
	private ResponseEntity<?> createSuccessfulRedirectToStatistic(ShortURL url) {
		// En l tienes todos los datos de la shortURL
		String resultado = "Este enlace ha recibido " + clickRepository.countByHash(url.getHash()) + " clicks";
		resultado += "</br>La url es " + url.getTarget();
		resultado += "</br>La fecha de creación es " + url.getDate().toString();
		return new ResponseEntity<>(resultado, HttpStatus.OK);
	}

	/**
	 * Devuelve sugerencias para una URL personalizada que ya existe
	 * @param url URL sobre la que se esta escribiendo una etiqueta
	 * @param personalizada Etiqueta para dicha URL
	 * @return Lista de sugerencias o vacio si la etiqueta no esta cogida
	 */
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

	/**
	 * Redirige a la pagina de estadisticas
	 * @param id Hash o etiqueta de la URL
	 * @param request Peticion
	 * @return Pagina de estadisticas
	 */
	@RequestMapping(value = "/{id:(?!link|index).*}+", method = RequestMethod.GET)
	public ResponseEntity<?> redirectToEstadisticas(@PathVariable String id, HttpServletRequest request) {
		logger.info("Requested redirection with hash " + id);
		ShortURL l = shortURLRepository.findByHash(id);
		if (l != null) {
			createAndSaveClick(id, HttpServletRequestUtils.getBrowser(request),
					HttpServletRequestUtils.getPlatform(request), HttpServletRequestUtils.getRemoteAddr(request));
			return createSuccessfulRedirectToStatistic(l);
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
			ShortURL urlconID = shortURLRepository.findByHash(customTag);
			if (urlconID != null) {
				// la url personalizada ya existe
				String SugerenciaSufijo = Sugerencias.sugerenciaSufijos(shortURLRepository, customTag);
				String SugerenciaSufijo2 = SugerenciaSufijo;
				while (SugerenciaSufijo2.equals(SugerenciaSufijo)) {
					SugerenciaSufijo2 = Sugerencias.sugerenciaSufijos(shortURLRepository, customTag);
				}
				// las recomendaciones se separan con el separador ":"
				throw new Error400Response("La URL a personalizar ya existe:" + SugerenciaSufijo + ":" + SugerenciaSufijo2);
			}
		}
		ShortURL su = createAndSaveIfValid(url, customTag, ads);
		if (su != null) {
			HttpHeaders h = new HttpHeaders();
			h.setLocation(su.getUri());
			return new ResponseEntity<>(su, h, HttpStatus.CREATED);
		} else {
			throw new Error400Response("La URL a acortar no es válida");
		}
	}
}
