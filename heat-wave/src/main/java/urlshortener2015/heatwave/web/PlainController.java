package urlshortener2015.heatwave.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;

import urlshortener2015.heatwave.entities.DetailedStats;
import urlshortener2015.heatwave.entities.ClientFilterMessage;
import urlshortener2015.heatwave.entities.ShortURL;
import urlshortener2015.heatwave.repository.ClickRepository;
import urlshortener2015.heatwave.repository.ShortURLRepository;
import urlshortener2015.heatwave.utils.ClickUtils;
import urlshortener2015.heatwave.utils.HttpServletRequestUtils;
import urlshortener2015.heatwave.utils.SecurityContextUtils;

@Controller
public class PlainController {

	private static final Logger logger = LoggerFactory.getLogger(PlainController.class);

	@Autowired
	private ShortURLRepository shortURLRepository;

	@Autowired
	private ClickRepository clickRepository;

	@Autowired
	private SimpMessagingTemplate template;
	
	@Autowired
	private ConnectionRepository connectionRepository;
	
	/**
	 * Redirects to the home page
	 * @param request Request
	 * @param model Model
	 * @return Home page
	 */
	@RequestMapping(value = "/")
	public String homePage(HttpServletRequest request, Model model){
		model.addAttribute("authThrough", SecurityContextUtils.getAuthThrough(SecurityContextHolder.getContext(), connectionRepository));
		model.addAttribute("authAs", SecurityContextUtils.getAuthAs(SecurityContextHolder.getContext(), connectionRepository));
		logger.info("authThrough: " + (String) model.asMap().get("authThrough"));
		logger.info("authAs: " + (String) model.asMap().get("authAs"));
		return MainController.DEFAULT_HOME_PATH;
	}
	
	/**
	 * Redirect to a shortened URL
	 * @param id Hash or custom tag
	 * @param request Request
	 * @param model Model
	 * @return Redirect page
	 * @throws IOException
	 */
	@RequestMapping(value = "/{id:(?!link|!stadistics|!error|index).*}", method = RequestMethod.GET)
	public String redirectTo(@PathVariable String id, HttpServletRequest request, Model model) throws IOException {
		logger.info("Requested redirection to statistics with hash " + id);
		ShortURL url = shortURLRepository.findByHash(id);
		if (url != null) {
			MainController.createAndSaveClick(id, HttpServletRequestUtils.getBrowser(request),
					HttpServletRequestUtils.getPlatform(request), HttpServletRequestUtils.getRemoteAddr(request),
					HttpServletRequestUtils.getCountry(request), clickRepository);
			model.addAttribute("targetURL", url.getTarget());
			model.addAttribute("countDown", MainController.DEFAULT_COUNTDOWN);
			model.addAttribute("advertisement", MainController.DEFAULT_AD_PATH);
			model.addAttribute("enableAds", url.getAds());
			return MainController.DEFAULT_REDIRECTING_PATH;
		} else {
			throw new HttpClientErrorException(HttpStatus.NOT_FOUND, MainController.DEFAULT_URL_NOT_FOUND_MESSAGE);
		}
	}

	/**
	 * Redirects to the stats page
	 * @param id Hash or custom tag
	 * @param request Request
	 * @param model Model
	 * @return Stats page
	 */
	@RequestMapping(value = "/{id:(?!link|!stadistics|!error|index).*}+", method = RequestMethod.GET)
	public String redirectToEstadisticas(@PathVariable String id, HttpServletRequest request, Model model) {
		logger.info("Requested redirection with hash " + id);
		ShortURL url = shortURLRepository.findByHash(id);
		if (url != null) {
			DetailedStats detailedStats = ClickUtils.fromMapToChartParams(url, clickRepository.aggregateInfoByHash(id));
			model.addAttribute("detailedStats", detailedStats);
			model.addAttribute("detailedStatsJSON", detailedStats.asJSON());
			model.addAttribute("hash", id);
			return MainController.DEFAULT_STATS_PATH;
		} else {
			model.addAttribute("errorCause", MainController.DEFAULT_URL_NOT_FOUND_MESSAGE);
			return MainController.DEFAULT_ERROR_PATH;
		}
	}

	/**
	 * Redirects to the rules page
	 * @param id Hash or custom tag
	 * @param request Request
	 * @param model Model
	 * @return Rules page
	 */
	@RequestMapping(value = "/{id:(?!link|!stadistics|index).*}/rules", method = RequestMethod.GET)
	public String redirectToRules(@PathVariable String id, HttpServletRequest request, Model model) {
		String respuesta = "rules";
		logger.info("Requested redirection with hash " + id);
		ShortURL l = shortURLRepository.findByHash(id);
		if (l != null) {
			model.addAttribute("reglas", l.getRules());
			model.addAttribute("urlId", id);
			return respuesta;
		} else {
			throw new HttpClientErrorException(HttpStatus.NOT_FOUND, MainController.DEFAULT_URL_NOT_FOUND_MESSAGE);
		}
	}

	/**
	 * Handles message receipts over the WebSocket
	 * @param filter Message received
	 * @throws Exception
	 */
	@MessageMapping("/statistics")
	public void respuestaSocket(ClientFilterMessage filter) throws Exception {
		this.template.convertAndSend("/sockets/" + filter.getId(), clickRepository.aggregateInfoByHash(filter.getId(), filter.getFrom(), filter.getTo()));
	}
}
