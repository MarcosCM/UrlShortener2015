package urlshortener2015.heatwave.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
	
	@Autowired
	private ShortURLRepository shortURLRepository;
	
	@Autowired
	private ClickRepository clickRepository;
	
	@Autowired
	private SimpMessagingTemplate template;
	
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
			MainController.createAndSaveClick(id, HttpServletRequestUtils.getBrowser(request),
					HttpServletRequestUtils.getPlatform(request), HttpServletRequestUtils.getRemoteAddr(request), clickRepository);
			// BasicStats stats = new BasicStats(clickRepository.countByHash(url.getHash()), url.getTarget(), url.getDate().toString());
			// this.template.convertAndSend("/sockets/"+id, new
			// Greeting(resultado));
			// this.template.convertAndSend("/sockets/" + id, stats);
			model.addAttribute("targetURL", url.getTarget());
			model.addAttribute("countDown", MainController.DEFAULT_COUNTDOWN);
			model.addAttribute("advertisement", MainController.DEFAULT_AD_PATH);
			model.addAttribute("enableAds", url.getAds());
			return MainController.DEFAULT_REDIRECTING_PATH;
		} else {
			throw new Error400Response(MainController.DEFAULT_URL_NOT_FOUND_MESSAGE);
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
			return MainController.DEFAULT_STATS_PATH;
		} else {
			model.addAttribute("errorCause", MainController.DEFAULT_URL_NOT_FOUND_MESSAGE);
			return MainController.DEFAULT_ERROR_PATH;
		}
	}
}
