package urlshortener2015.heatwave.web;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

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
import org.springframework.web.client.HttpClientErrorException;

import com.google.common.hash.Hashing;

import urlshortener2015.heatwave.entities.Click;
import urlshortener2015.heatwave.entities.BasicStats;
import urlshortener2015.heatwave.entities.HelloMessage;
import urlshortener2015.heatwave.entities.ShortURL;
import urlshortener2015.heatwave.entities.Suggestion;
import urlshortener2015.heatwave.repository.ClickRepository;
import urlshortener2015.heatwave.repository.ShortURLRepository;
import urlshortener2015.heatwave.utils.HttpServletRequestUtils;
import urlshortener2015.heatwave.utils.SuggestionUtils;
import org.springframework.messaging.handler.annotation.MessageMapping;

@RestController
public class MainController {

	private static final Logger logger = LoggerFactory.getLogger(MainController.class);

	@Autowired
	private ShortURLRepository shortURLRepository;

	@Autowired
	private ClickRepository clickRepository;

	// Times
	public static final int DEFAULT_COUNTDOWN = 10;
	// Paths
	public static final String DEFAULT_AD_PATH = "./images/header.png";
	public static final String DEFAULT_HOME_PATH = "index";
	public static final String DEFAULT_REDIRECTING_PATH = "redirecting";
	public static final String DEFAULT_STATS_PATH = "stats";
	public static final String DEFAULT_ERROR_PATH = "error";
	// Error messages
	public static final String DEFAULT_URL_NOT_FOUND_MESSAGE = "That URL does not exist";

	/**
	 * Saves a click
	 * @param hash Shortened URL hash (or custom tag)
	 * @param browser Source browser
	 * @param platform Source platform
	 * @param ip Source IP
	 * @param country Source country
	 * @param clickRepository Clicks repository
	 */
	public static void createAndSaveClick(String hash, String browser, String platform, String ip, String country, ClickRepository clickRepository) {
		Click cl = new Click(null, hash, new Date(System.currentTimeMillis()), browser, platform, ip, country);
		cl = clickRepository.insert(cl);
		logger.info(cl != null ? "Click on [" + hash + "] saved with id [" + cl.getId() + "]"
				: "[" + hash + "] was not saved");
	}

	/**
	 * Creates a shortened URL
	 * @param url URL to shorten
	 * @param customTag Custom tag
	 * @param ads Enable/Disable advertisements
	 * @param users List of users authorized not to see advertisements
	 * @param shortURLRepository Shortened URLs repository
	 * @param rule 
	 * @return Shortened URL if success, otherwise error
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public static ShortURL createAndSaveIfValid(String url, String customTag, Boolean ads, Map<String, List<String>> users, ShortURLRepository shortURLRepository, String rule)
					throws MalformedURLException, URISyntaxException {
		UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" });
		if (urlValidator.isValid(url)) {
			String id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
			if (customTag != null && !customTag.equals("")) {
				id = customTag;
			}

			// Si ya existe devolver null
			ShortURL su = new ShortURL(id, url, new URI(id), new Date(System.currentTimeMillis()),
					HttpStatus.TEMPORARY_REDIRECT.value(), true, ads, users);
			su.addRule(rule);
			return shortURLRepository.insert(su);
		} else {
			return null;
		}
	}

	public static ResponseEntity<?> createSuccessfulRedirectToStatisticJson(ShortURL url,
			ClickRepository clickRepository) {
		// En l tienes todos los datos de la shortURL
		BasicStats stats = new BasicStats(clickRepository.countByHash(url.getHash()), url.getTarget(),
				url.getDate().toString());
		return new ResponseEntity<>(stats, HttpStatus.OK);
	}

	private static ArrayList<Suggestion> listaSugerencias(String customTag, ShortURLRepository shortURLRepository) {
		ArrayList<Suggestion> lista = new ArrayList<Suggestion>();
		if (customTag != null && !customTag.equals("") && shortURLRepository.findByHash(customTag) != null) {
			String sugerenciaSufijo = SuggestionUtils.sugerenciaSufijos(shortURLRepository, customTag);
			String sugerenciaSufijo2 = sugerenciaSufijo;

			while (sugerenciaSufijo2.equals(sugerenciaSufijo)) {
				sugerenciaSufijo2 = SuggestionUtils.sugerenciaSufijos(shortURLRepository, customTag);
			}

			lista.add(new Suggestion(sugerenciaSufijo2));
			lista.add(new Suggestion(sugerenciaSufijo));
			lista = SuggestionUtils.sugerenciasFromAPIs(lista, customTag, shortURLRepository);
		}
		return lista;
	}

	/**
	 * Gets suggestions of a custom URL tag if it already exists
	 * @param url URL wanted to be shortened
	 * @param customTag Custom tag
	 * @return List of suggestions if the URL already exists, otherwise empty list
	 */
	@RequestMapping(value = "/sugerencias/recomendadas", method = RequestMethod.GET)
	public ResponseEntity<ArrayList<Suggestion>> sugerencias(@RequestParam(value = "url", required = false) String url,
			@RequestParam(value = "customTag", required = false) String customTag) {
		ArrayList<Suggestion> lista = MainController.listaSugerencias(customTag, shortURLRepository);
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	@RequestMapping(value = "/{id:(?!link|!stadistics|!error||index).*}+/json", method = RequestMethod.GET)
	public ResponseEntity<?> redirectToEstadisticasJson(@PathVariable String id, HttpServletRequest request) {
		logger.info("Requested redirection with hash " + id);
		ShortURL l = shortURLRepository.findByHash(id);
		if (l != null) {
			return MainController.createSuccessfulRedirectToStatisticJson(l, clickRepository);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Shortens a specific URL
	 * @param url URL to shorten
	 * @param customTag Custom tag of the shortened URL
	 * @param enableAd Enables/Disables advertisements
	 * @param request Servlet Request
	 * @return Success message if the shortened URL was created, error message otherwise
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	@RequestMapping(value = "/link", method = RequestMethod.POST)
	public ResponseEntity<ShortURL> shortener(@RequestParam("url") String url,
			@RequestParam(value = "customTag", required = false) String customTag,
			@RequestParam(value = "enableAd", required = false) Boolean enableAd,
			@RequestParam(value = "rule", required = false) String rule, HttpServletRequest request)
					throws MalformedURLException, URISyntaxException {
		logger.info("Requested new short for uri " + url);
		// Get users array
		Map<String, List<String>> users = HttpServletRequestUtils.getUsers(request);
		// Get ads enabling
		Boolean ads;
		if (enableAd != null && enableAd) ads = new Boolean(true);
		else ads = new Boolean(false);

		if (customTag != null && !customTag.equals("")) {
			ShortURL urlConID = shortURLRepository.findByHash(customTag);
			if (urlConID != null) {
				// That custom tag already exists
				return new ResponseEntity<>(
						new ShortURL(customTag, url, new URI(customTag), new Date(System.currentTimeMillis()), HttpStatus.TEMPORARY_REDIRECT.value(), true, enableAd, users),
						HttpStatus.BAD_REQUEST);
			}
		}

		ShortURL su = MainController.createAndSaveIfValid(url, customTag, ads, users, shortURLRepository, rule);
		if (su != null) {
			HttpHeaders h = new HttpHeaders();
			h.setLocation(su.getUri());
			return new ResponseEntity<>(su, h, HttpStatus.CREATED);
		} else {
			// Invalid target URL
			return new ResponseEntity<>(su, HttpStatus.BAD_REQUEST);
		}
	}

	// A donde llega los mensajes de los sockets desde el cliente
	@MessageMapping("/stadistics")
	// @SendTo("/sockets/IDPARAMANDAR")
	public BasicStats respuestaSocket(HelloMessage infoQueLlega) throws Exception {
		return new BasicStats(new Long(1), "", "");
	}
}
