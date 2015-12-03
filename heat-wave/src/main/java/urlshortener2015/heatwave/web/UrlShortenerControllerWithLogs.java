package urlshortener2015.heatwave.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.UUID;

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

import urlshortener2015.common.domain.Click;
import urlshortener2015.common.domain.ShortURL;
import urlshortener2015.common.repository.ClickRepository;
import urlshortener2015.common.repository.ShortURLRepository;

@RestController
public class UrlShortenerControllerWithLogs {

	private static final Logger logger = LoggerFactory.getLogger(UrlShortenerControllerWithLogs.class);

	@Autowired
	protected ShortURLRepository shortURLRepository;

	@Autowired
	protected ClickRepository clickRepository;

	protected void createAndSaveClick(String hash, String ip) {
		Click cl = new Click(null, hash, new Date(System.currentTimeMillis()), null, null, null, ip, null);
		cl = clickRepository.save(cl);
		logger.info(cl != null ? "[" + hash + "] saved with id [" + cl.getId() + "]" : "[" + hash + "] was not saved");
	}

	protected String extractIP(HttpServletRequest request) {
		return request.getRemoteAddr();
	}

	protected ResponseEntity<?> createSuccessfulRedirectToResponse(ShortURL l) {
		HttpHeaders h = new HttpHeaders();
		h.setLocation(URI.create(l.getTarget()));
		return new ResponseEntity<>(h, HttpStatus.valueOf(l.getMode()));
	}

	@RequestMapping(value = "/{id:(?!link|index).*}", method = RequestMethod.GET)
	public ResponseEntity<?> redirectTo(@PathVariable String id, HttpServletRequest request) {
		logger.info("Requested redirection with hash " + id);
		ShortURL l = shortURLRepository.findByKey(id);
		if (l != null) {
			createAndSaveClick(id, extractIP(request));
			return createSuccessfulRedirectToResponse(l);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "/link", method = RequestMethod.POST)
	public ResponseEntity<ShortURL> shortener(@RequestParam("url") String url,
			@RequestParam("personalizada") String personalizada,
			@RequestParam(value = "sponsor", required = false) String sponsor,
			@RequestParam(value = "brand", required = false) String brand, HttpServletRequest request) {
		logger.info("Requested new short for uri " + url);
		if(personalizada != null && !personalizada.equals("")){
			ShortURL urlconID = shortURLRepository.findByKey(personalizada);
			if(urlconID!=null){
				//la url personalizada ya existe
				HttpHeaders h = new HttpHeaders();
				h.add("Personalizada", "La url a personalizada ya existe");

				return new ResponseEntity<>(h,HttpStatus.NOT_ACCEPTABLE);			
			}
		}
		ShortURL su = createAndSaveIfValid(url, personalizada, sponsor, brand, UUID.randomUUID().toString(),
				extractIP(request));
		if (su != null) {
			HttpHeaders h = new HttpHeaders();
			h.setLocation(su.getUri());
			return new ResponseEntity<>(su, h, HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	protected ShortURL createAndSaveIfValid(String url, String personalizada, String sponsor, String brand,
			String owner, String ip) {
		UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" });
		if (urlValidator.isValid(url)) {

			String id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
			if (personalizada != null && !personalizada.equals("")) {
				id = personalizada;
			}
			// si ya existe devoler null

				ShortURL su = new ShortURL(id, url,
						linkTo(methodOn(UrlShortenerControllerWithLogs.class).redirectTo(id, null)).toUri(), sponsor,
						new Date(System.currentTimeMillis()), owner, HttpStatus.TEMPORARY_REDIRECT.value(), true, ip,
						null);
				return shortURLRepository.save(su);
			

		} else {
			return null;
		}
	}
}
