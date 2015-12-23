package urlshortener2015.heatwave.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import urlshortener2015.heatwave.entities.DetailedStats;
import urlshortener2015.heatwave.entities.ShortURL;
import urlshortener2015.heatwave.exceptions.Error400Response;
import urlshortener2015.heatwave.repository.ClickRepository;
import urlshortener2015.heatwave.repository.ShortURLRepository;
import urlshortener2015.heatwave.utils.HttpServletRequestUtils;

@Controller
public class PlainController {

	private static final Logger logger = LoggerFactory.getLogger(PlainController.class);
	
	private static final int DEFAULT_COUNTDOWN = 10;
	// Paths
	private static final String DEFAULT_AD_PATH = "./images/header.png";
	private static final String DEFAULT_REDIRECTING_PATH = "redirecting";
	private static final String DEFAULT_STATS_PATH = "stats";
	private static final String DEFAULT_ERROR_PATH = "error";
	// Error messages
	private static final String DEFAULT_URL_NOT_FOUND_MESSAGE = "That URL does not exist";
	
	@Autowired
	private ShortURLRepository shortURLRepository;
	
	@Autowired
	private ClickRepository clickRepository;
	
	@Autowired
	private SimpMessagingTemplate template;
	
	@Autowired
	private Facebook facebook;
	
	/**
	 * Redireccion de una URL acortada
	 * @param id Hash o etiqueta de la URL
	 * @param request Peticion
	 * @param model Modelo con atributos
	 * @return Pagina de redireccion
	 * @throws IOException Si el archivo que contiene la imagen de publicidad no existe o no es una imagen
	 */
	@RequestMapping(value = "/{id:(?!link|!stadistics|index).*}", method = RequestMethod.GET)
	public String redirectTo(@PathVariable String id, HttpServletRequest request, Model model) throws IOException {
		logger.info("Requested redirection to statistics with hash " + id);
		ShortURL url = shortURLRepository.findByHash(id);
		
		if (url != null) {
			UrlShortenerControllerWithLogs.createAndSaveClick(id, HttpServletRequestUtils.getBrowser(request),
					HttpServletRequestUtils.getPlatform(request), HttpServletRequestUtils.getRemoteAddr(request), clickRepository);
			// BasicStats stats = new BasicStats(clickRepository.countByHash(url.getHash()), url.getTarget(), url.getDate().toString());
			// this.template.convertAndSend("/sockets/"+id, new
			// Greeting(resultado));
			// this.template.convertAndSend("/sockets/" + id, stats);
			model.addAttribute("targetURL", url.getTarget());
			model.addAttribute("countDown", DEFAULT_COUNTDOWN);
			model.addAttribute("advertisement", DEFAULT_AD_PATH);
			model.addAttribute("enableAds", url.getAds());
			return DEFAULT_REDIRECTING_PATH;
		} else {
			throw new Error400Response(DEFAULT_URL_NOT_FOUND_MESSAGE);
		}
	}
	
	@RequestMapping(value = "/connect/facebookConnected", method = RequestMethod.GET)
	public String connectedToFacebook(HttpServletRequest request, Model model){
		model.addAttribute("targetURL", facebook.userOperations().getUserProfile().getEmail());
		model.addAttribute("countDown", DEFAULT_COUNTDOWN);
		model.addAttribute("advertisement", DEFAULT_AD_PATH);
		model.addAttribute("enableAds", true);
		return "redirecting";
	}
	
	/**
	 * Autenticacion logeando a traves de Facebook
	 * @param id Hash o etiqueta de la URL
	 * @param request Peticion
	 * @param model Modelo con atributos
	 * @return Pagina de redireccion
	 * @throws IOException Si el archivo que contiene la imagen de publicidad no existe o no es una imagen
	 */
	@RequestMapping(value = "/login/facebook/{id:(?!link|!stadistics|index).*}", method = RequestMethod.GET)
	public String facebookRedirectTo(@PathVariable String id, HttpServletRequest request, Model model){
		ShortURL url = shortURLRepository.findByHash(id);
		logger.info("is Facebook obj null? " + (facebook == null));
		if (url != null){
			// The user will be redirected
			model.addAttribute("targetURL", url.getTarget());
			model.addAttribute("countDown", DEFAULT_COUNTDOWN);
			model.addAttribute("advertisement", DEFAULT_AD_PATH);
			if (url.getAds()){
				if (facebook.isAuthorized()){
					List<String> mails;
					if (url.getUsers() != null && (mails = url.getUsers().get("facebook")) != null){
						String userMail = facebook.userOperations().getUserProfile().getEmail();
						for(String mail : mails){
							if (mail.equals(userMail)){
								logger.info("The user IS in the users list");
								model.addAttribute("enableAds", false);
								return DEFAULT_REDIRECTING_PATH;
							}
						}
					}
					// There is no Facebook users in that URL or the user is not in its Facebook users
					logger.info("The user IS NOT in the users list");
					model.addAttribute("enableAds", true);
					return DEFAULT_REDIRECTING_PATH;
				}
				else{
					logger.info("User DOES NOT authorize the app");
					// Facebook user does not authorize the app
					// redirect to request mapped by ConnectController
					return "redirect:/connect/facebook";
				}
			}
			else{
				// Ads are not enabled in the URL
				model.addAttribute("enableAds", false);
				return DEFAULT_REDIRECTING_PATH;
			}
		}
		else{
			// URL not found
			throw new Error400Response(DEFAULT_URL_NOT_FOUND_MESSAGE);
		}
	}
	
	/**
	 * Redirige a la pagina de estadisticas
	 * @param id Hash o etiqueta de la URL
	 * @param request Peticion
	 * @param model Modelo con atributos
	 * @return Pagina de estadisticas
	 */
	@RequestMapping(value = "/{id:(?!link|!stadistics|index).*}+", method = RequestMethod.GET)
	public String redirectToEstadisticas(@PathVariable String id, HttpServletRequest request, Model model) {
		logger.info("Requested redirection with hash " + id);
		ShortURL url = shortURLRepository.findByHash(id);
		if (url != null) {
			//DetailedStats detailedStats = ClickUtils.fromMapToChartParams(url, clickRepository.aggregateInfoByHash(id));
			// INICIO TESTEO
			Map<String, Integer> data = new HashMap<String, Integer>();
			data.put("Firefox", 5);
			data.put("Chrome", 10);
			data.put("Opera", 2);
			Map<String, String> options = new HashMap<String, String>();
			options.put("title",  "By Browser");
			String type = "PieChart";
			DetailedStats.ChartData chartData = new DetailedStats.ChartData(data, options, type);
			Map<String, DetailedStats.ChartData> charts = new HashMap<String, DetailedStats.ChartData>();
			charts.put("Browser", chartData);
			DetailedStats detailedStats = new DetailedStats(url, charts);
			// FIN TESTEO
			model.addAttribute("detailedStats", detailedStats);
			return DEFAULT_STATS_PATH;
		} else {
			model.addAttribute("errorCause", DEFAULT_URL_NOT_FOUND_MESSAGE);
			return DEFAULT_ERROR_PATH;
		}
	}
}
