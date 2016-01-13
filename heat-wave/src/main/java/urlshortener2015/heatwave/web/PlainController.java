package urlshortener2015.heatwave.web;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.google.api.Google;
import org.springframework.social.twitter.api.Twitter;
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
import urlshortener2015.heatwave.utils.ShortURLUtils;

@Controller
public class PlainController {

	private static final Logger logger = LoggerFactory.getLogger(PlainController.class);

	@Autowired
	private Facebook facebook;

	@Autowired
	private Twitter twitter;

	@Autowired
	private Google google;
	
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
			// Check if the user is in the authorized users list not to see ads
			boolean enableAds;
			SecurityContext securityContext = SecurityContextHolder.getContext();
			String authThrough = SecurityContextUtils.getAuthThrough(securityContext, connectionRepository);
			String authAs = SecurityContextUtils.getAuthAs(securityContext, connectionRepository);
			switch(authThrough){
				case "local":
					enableAds = !ShortURLUtils.isUserInList(url, authAs);
					break;
				case "twitter":
					enableAds = !ShortURLUtils.isUserInList(url, twitter);
					break;
				case "facebook":
					enableAds = !ShortURLUtils.isUserInList(url, facebook);
					break;
				case "google":
					enableAds = !ShortURLUtils.isUserInList(url, google);
					break;
				default:
					enableAds = true;
					break;
			}
			
			MainController.createAndSaveClick(id, HttpServletRequestUtils.getBrowser(request),
					HttpServletRequestUtils.getPlatform(request), HttpServletRequestUtils.getRemoteAddr(request),
					HttpServletRequestUtils.getCountry(request), clickRepository);
			model.addAttribute("targetURL", url.getTarget());
			model.addAttribute("countDown", MainController.DEFAULT_COUNTDOWN);
			model.addAttribute("advertisement", MainController.DEFAULT_AD_PATH);
			model.addAttribute("enableAds", enableAds);
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
		logger.info("Requested stats with hash " + id);
		ShortURL url = shortURLRepository.findByHash(id);
		if (url != null) {
			// Only the creator of the shortened URL can access this section
			SecurityContext securityContext = SecurityContextHolder.getContext();
			if (!SecurityContextUtils.isCreator(url, securityContext, connectionRepository)) throw new HttpClientErrorException(HttpStatus.FORBIDDEN, MainController.DEFAULT_FORBIDDEN_ACTION);
			
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
		ShortURL url = shortURLRepository.findByHash(id);
		if (url != null) {
			// Only the creator of the shortened URL can access this section
			SecurityContext securityContext = SecurityContextHolder.getContext();
			if (!SecurityContextUtils.isCreator(url, securityContext, connectionRepository)) throw new HttpClientErrorException(HttpStatus.FORBIDDEN, MainController.DEFAULT_FORBIDDEN_ACTION);
			
			model.addAttribute("reglas", url.getRules());
			model.addAttribute("urlId", id);
			return respuesta;
		} else {
			throw new HttpClientErrorException(HttpStatus.NOT_FOUND, MainController.DEFAULT_URL_NOT_FOUND_MESSAGE);
		}
	}
	
	@RequestMapping(value = "/{id:(?!link|!stadistics|index).*}/update_rules", method = RequestMethod.POST)
	public String updateRules(@PathVariable String id, HttpServletRequest request, Model model) {
		String respuesta = "rules";
		logger.info("Requested redirection with hash " + id);
		ShortURL url = shortURLRepository.findByHash(id);
		
		if (url != null){
			// Only the creator of the shortened URL can access this section
			SecurityContext securityContext = SecurityContextHolder.getContext();
			if (!SecurityContextUtils.isCreator(url, securityContext, connectionRepository)) throw new HttpClientErrorException(HttpStatus.FORBIDDEN, MainController.DEFAULT_FORBIDDEN_ACTION);
			
			Enumeration<String> e = request.getParameterNames();
			while(e.hasMoreElements()){
				String nameAtr = e.nextElement();
				
				// Si es un checkbox
				if(nameAtr.substring(0, nameAtr.indexOf("_")).equals("delete")){
					Object o = request.getParameter(nameAtr);
					if(o != null && ((String) o).equals("on"))
						url.deleteRule(Integer.parseInt(nameAtr.substring(nameAtr.indexOf("_") + 1, nameAtr.length())));
				}
				
				// Si es un textarea de reglas modificadas
				else if(nameAtr.substring(0, nameAtr.indexOf("_")).equals("rule")){
					String script = (String) request.getParameter(nameAtr);
					if(script != null && !script.equals("") && !script.equals(" "))
						url.modifyRule(Integer.parseInt(nameAtr.substring(nameAtr.indexOf("_") + 1, nameAtr.length())), script);
				}
			}
			
			shortURLRepository.save(url);
			
			model.addAttribute("reglas", url.getRules());
			model.addAttribute("urlId", id);
			return respuesta;
		}
		else throw new HttpClientErrorException(HttpStatus.NOT_FOUND, MainController.DEFAULT_URL_NOT_FOUND_MESSAGE);
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
