package urlshortener2015.heatwave.utils;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class HttpServletRequestUtils {

	private static final Logger logger = LoggerFactory.getLogger(HttpServletRequestUtils.class);

	public static String getRemoteAddr(HttpServletRequest request) {
		return request != null ? request.getRemoteAddr() : null;
	}

	public static String getUserAgent(HttpServletRequest request) {
		return request != null ? request.getHeader("User-Agent") : null;
	}

	public static String getPlatform(HttpServletRequest request) {
		if (request != null) {
			String userAgent = HttpServletRequestUtils.getUserAgent(request).toLowerCase();
			if (userAgent.matches(".*windows.*"))
				return "Windows";
			else if (userAgent.matches(".*unix.*") || userAgent.matches(".*linux.*"))
				return "Unix";
			else if (userAgent.matches(".*mac.*"))
				return "Mac OS";
			else
				return "Unknown";
		} else
			return null;
	}

	public static String getBrowser(HttpServletRequest request) {
		if (request != null) {
			String userAgent = HttpServletRequestUtils.getUserAgent(request).toLowerCase();
			if (userAgent.matches(".*opera.*"))
				return "Opera";
			else if (userAgent.matches(".*edge.*"))
				return "Microsoft Edge";
			else if (userAgent.matches(".*chromium.*"))
				return "Chromium";
			else if (userAgent.matches(".*safari.*"))
				return "Safari";
			else if (userAgent.matches(".*explorer.*"))
				return "Internet Explorer";
			else if (userAgent.matches(".*firefox.*"))
				return "Firefox";
			else if (userAgent.matches(".*chrome.*"))
				return "Chrome";
			else
				return "Unknown";
		} else
			return null;
	}

	public static String getCountry(HttpServletRequest request) {
		System.out.println("getcountry");
		String ip = HttpServletRequestUtils.getRemoteAddr(request);
		RestTemplate restTemplate = new RestTemplate();
		String country="LocalHost";
		try{
		ResponseEntity<String> response = restTemplate.getForEntity("http://ip-api.com/json/" + ip,
				String.class);
		String body = response.getBody();
		String[] partes = body.split(",");
		int i = 0;
		while (partes.length > i && !partes[i].contains("\"country\"")) {
			i++;
			System.out.println(partes[i]);
		}
		partes = partes[i].split(":");
		country=partes[1].replace("\"", "");
		}catch(Exception a){}
		return country;
	}

	public static Map<String, List<String>> getUsers(HttpServletRequest request) {
		Enumeration<String> paramNames = request.getParameterNames();
		Map<String, List<String>> res = new HashMap<String, List<String>>();
		String paramName, currentParam;
		while (paramNames.hasMoreElements()) {
			paramName = paramNames.nextElement();
			logger.info("Checking param: " + paramName);
			// users["gmail"][]
			if (paramName.startsWith("users[")) {
				// gmail
				currentParam = paramName.substring(paramName.indexOf("[") + 2, paramName.indexOf("]") - 1);
				res.put(currentParam, Arrays.asList(request.getParameterValues(paramName)));
			}
		}

		return res;
	}
}
