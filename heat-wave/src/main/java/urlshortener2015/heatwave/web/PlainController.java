package urlshortener2015.heatwave.web;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

import urlshortener2015.heatwave.entities.Click;
import urlshortener2015.heatwave.entities.DetailedStats;
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
			DetailedStats stats = graficoStats(id, null, null);
			this.template.convertAndSend("/sockets/" + id, stats);
			model.addAttribute("targetURL", url.getTarget());
			model.addAttribute("countDown", MainController.DEFAULT_COUNTDOWN);
			model.addAttribute("advertisement", MainController.DEFAULT_AD_PATH);
			model.addAttribute("enableAds", url.getAds());
			return MainController.DEFAULT_REDIRECTING_PATH;
		} else {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, MainController.DEFAULT_URL_NOT_FOUND_MESSAGE);
		}
	}

	/**
	 * Redirects to the stats page
	 * @param id Hash or custom tag
	 * @param request Request
	 * @param model Model
	 * @return Stats page
	 */
	@RequestMapping(value = "/{id:(?!link|!stadistics|!error||index).*}+", method = RequestMethod.GET)
	public String redirectToEstadisticas(@PathVariable String id, HttpServletRequest request, Model model) {
		logger.info("Requested redirection with hash " + id);
		ShortURL url = shortURLRepository.findByHash(id);
		if (url != null) {
			DetailedStats detailedStats = ClickUtils.fromMapToChartParams(url, clickRepository.aggregateInfoByHash(id));
			model.addAttribute("detailedStats", detailedStats);
			return MainController.DEFAULT_STATS_PATH;
		} else {
			model.addAttribute("errorCause", MainController.DEFAULT_URL_NOT_FOUND_MESSAGE);
			return MainController.DEFAULT_ERROR_PATH;
		}
	}

	@RequestMapping(value = "/stats/Filtradas", method = RequestMethod.GET)
	public ResponseEntity<?> estadiscticasFiltrada(@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "desde", required = true) String desde,
			@RequestParam(value = "hasta", required = true) String hasta) {
		DetailedStats detailedStats = graficoStats(id, desde, hasta);

		return new ResponseEntity<>(detailedStats, HttpStatus.OK);
	}

	/*
	 * Devuelve un mapa de Pais-num clicks, dada (opcional) dos fechas para filtrar
	 */
	public Map<String, Integer> getCountries(List<Click> clicks, String desde, String hasta) {
		// mirar primero si es null
		Date ddesde = null;
		Date dhasta = null;
		if (hasta != null)
			dhasta = StringtoDate(hasta);
		if (desde != null)
			ddesde = StringtoDate(desde);

		Map<String, Integer> data = new HashMap<String, Integer>();
		for (int i = 0; i < clicks.size(); i++) {
			boolean valido = true;
			if (ddesde != null)
				valido &= (clicks.get(i).getDate().compareTo(ddesde) >= 0);
			if (dhasta != null)
				valido &= (clicks.get(i).getDate().compareTo(dhasta) <= 0);

			if (valido) {
				if (!data.containsKey(clicks.get(i).getCountry())) {
					data.put(clicks.get(i).getCountry(), 1);
				} else {
					int numero = data.get(clicks.get(i).getCountry()) + 1;
					data.remove(clicks.get(i).getCountry());
					data.put(clicks.get(i).getCountry(), numero);
				}
			}
		}
		return data;
	}

	public DetailedStats graficoStats(String id, String desde, String hasta) {
		ShortURL url = shortURLRepository.findByHash(id);
		List<Click> clicks = clickRepository.findByHash(id);
		Map<String, Integer> data = getCountries(clicks, desde, hasta);
		Map<String, String> options = new HashMap<String, String>();
		options.put("title", "By Browser");
		String type = "PieChart";
		DetailedStats.ChartData chartData = new DetailedStats.ChartData(data, options, type);
		Map<String, DetailedStats.ChartData> charts = new HashMap<String, DetailedStats.ChartData>();
		charts.put("Browser", chartData);
		DetailedStats detailedStats = new DetailedStats(url, charts);
		return detailedStats;
	}
	/*
	 * La clase Date de Java esta deprecada, asi que se necesita este metodo
	 */
	public static Date StringtoDate(String date) {
		Date nueva = null;
		try {
			String partes[] = date.split("-");
			nueva = asDate(LocalDate.of(Integer.valueOf(partes[0]), Integer.valueOf(partes[1]),
					Integer.valueOf(partes[2]) + 1));
		} catch (Exception a) {
		}
		return nueva;

	}

	public static Date asDate(LocalDate localDate) {
		return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}
}
