package urlshortener2015.heatwave.web;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import urlshortener2015.heatwave.entities.ShortURL;
import urlshortener2015.heatwave.exceptions.Error400Response;
import urlshortener2015.heatwave.repository.ShortURLRepository;

@Controller
public class PlainController {

	private static final Logger logger = LoggerFactory.getLogger(PlainController.class);
	
	private static final int DEFAULT_COUNTDOWN = 10;
	
	@Autowired
	private ShortURLRepository shortURLRepository;
	
	/**
	 * Redireccion de una URL acortada
	 * @param id Hash o etiqueta de la URL
	 * @param request Peticion
	 * @param model Modelo con atributos
	 * @return Pagina de redireccion
	 */
	@RequestMapping(value = "/{id:(?!link|index).*}", method = RequestMethod.GET)
	public String redirectTo(@PathVariable String id, HttpServletRequest request, Model model) {
		logger.info("Requested redirection to statistics with hash " + id);
		ShortURL url = shortURLRepository.findByHash(id);
		
		if (url != null) {
			model.addAttribute("targetURL", url.getTarget());
			model.addAttribute("countDown", DEFAULT_COUNTDOWN);
			return "redirecting";
		} else {
			throw new Error400Response("La URL no existe");
		}
	}
}
