package urlshortener2015.heatwave.utils;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class HttpServletRequestUtils {
	
	/**
	 * Gets the source IP address
	 * @param request Servlet Request
	 * @return Source IP Address
	 */
	public static String getRemoteAddr(HttpServletRequest request){
		return request != null ? request.getRemoteAddr() : null;
	}
	
	/**
	 * Gets the source User Agent
	 * @param request Servlet Request
	 * @return Source User Agent
	 */
	public static String getUserAgent(HttpServletRequest request){
		return request != null ? request.getHeader("User-Agent") : null;
	}
	
	/**
	 * Gets the source platform
	 * @param request Servlet Request
	 * @return Source platform
	 */
	public static String getPlatform(HttpServletRequest request){
		if (request != null){
			String userAgent = HttpServletRequestUtils.getUserAgent(request).toLowerCase();
			if (userAgent.matches(".*windows.*")) return "Windows";
			else if (userAgent.matches(".*unix.*") || userAgent.matches(".*linux.*")) return "Unix";
			else if (userAgent.matches(".*mac.*")) return "Mac OS";
			else return "Unknown";
		}
		else return null;
	}
	
	/**
	 * Gets the source browser
	 * @param request Servlet Request
	 * @return Source browser
	 */
	public static String getBrowser(HttpServletRequest request){
		if (request != null){
			String userAgent = HttpServletRequestUtils.getUserAgent(request).toLowerCase();
			if (userAgent.matches(".*opera.*")) return "Opera";
			else if (userAgent.matches(".*edge.*")) return "Microsoft Edge";
			else if (userAgent.matches(".*chromium.*")) return "Chromium";
			else if (userAgent.matches(".*chrome.*")) return "Chrome";
			else if (userAgent.matches(".*explorer.*")) return "Internet Explorer";
			else if (userAgent.matches(".*firefox.*")) return "Firefox";
			else if (userAgent.matches(".*safari.*")) return "Safari";
			else return "Unknown";
		}
		else return null;
	}
	
	/**
	 * Gets the source country
	 * @param request Servlet Request
	 * @return Source country
	 */
	public static String getCountry(HttpServletRequest request){
		String ip = HttpServletRequestUtils.getRemoteAddr(request);
		RestTemplate restTemplate = new RestTemplate();
		String country = "LocalHost";
		try{
			ResponseEntity<String> response = restTemplate.getForEntity("http://ip-api.com/json/" + ip, String.class);
			String body = response.getBody();
			String[] partes = body.split(",");
			int i = 0;
			while (partes.length > i && !partes[i].contains("\"country\"")) {
				i++;
			}
			partes = partes[i].split(":");
			country = partes[1].replace("\"", "");
		}catch(Exception a){}
		return country;
	}

	/**
	 * Gets the users list
	 * @param request Servlet Request
	 * @return Users list
	 */
	public static Map<String, List<String>> getUsers(HttpServletRequest request){
		Map<String, List<String>> res = new HashMap<String, List<String>>();
		
		Enumeration<String> paramNames = request.getParameterNames();
		String paramName, currentParam;
		while (paramNames.hasMoreElements()) {
			paramName = paramNames.nextElement();
			// users["twitter"][]
			if (paramName.startsWith("users[")) {
				// twitter
				currentParam = paramName.substring(paramName.indexOf("[") + 2, paramName.indexOf("]") - 1);
				res.put(currentParam, Arrays.asList(request.getParameterValues(paramName)));
			}
		}

		return res;
	}
}
