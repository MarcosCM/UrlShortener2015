package urlshortener2015.heatwave.utils;

import javax.servlet.http.HttpServletRequest;

public class HttpServletRequestUtils {
	
	public static String getRemoteAddr(HttpServletRequest request){
		return request.getRemoteAddr();
	}
	
	public static String getUserAgent(HttpServletRequest request){
		return request.getHeader("User-Agent");
	}
	
	public static String getPlatform(HttpServletRequest request){
		String userAgent = HttpServletRequestUtils.getUserAgent(request).toLowerCase();
		if (userAgent.matches(".*windows.*")) return "Windows";
		else if (userAgent.matches(".*unix.*") || userAgent.matches(".*linux.*")) return "Unix";
		else if (userAgent.matches(".*mac.*")) return "Mac OS";
		else return "Unknown";
	}
	
	public static String getBrowser(HttpServletRequest request){
		String userAgent = HttpServletRequestUtils.getUserAgent(request).toLowerCase();
		if (userAgent.matches(".*opera.*")) return "Opera";
		else if (userAgent.matches(".*edge.*")) return "Microsoft Edge";
		else if (userAgent.matches(".*chromium.*")) return "Chromium";
		else if (userAgent.matches(".*safari.*")) return "Safari";
		else if (userAgent.matches(".*explorer.*")) return "Internet Explorer";
		else if (userAgent.matches(".*firefox.*")) return "Firefox";
		else if (userAgent.matches(".*chrome.*")) return "Chrome";
		else return "Unknown";
	}
	
	public static String getCountry(HttpServletRequest request){
		//String ip = HttpServletRequestUtils.getRemoteAddr(request);
		return null;
	}
}
