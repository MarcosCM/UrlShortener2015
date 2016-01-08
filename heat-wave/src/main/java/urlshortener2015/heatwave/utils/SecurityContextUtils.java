package urlshortener2015.heatwave.utils;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.social.connect.ConnectionRepository;

public class SecurityContextUtils {

	/**
	 * Gets the method the user has been authenticated through
	 * @param securityContext Security Context
	 * @param connectionRepository Connection Repository
	 * @return Auth method
	 */
	public static String getAuthThrough(SecurityContext securityContext, ConnectionRepository connectionRepository){
		String authThrough = ApiBindingUtils.getAuthThrough(securityContext, connectionRepository);
		if (authThrough == null){
			authThrough = (securityContext.getAuthentication() == null) ? null : "local";
		}
		return authThrough;
	}
	
	/**
	 * Gets the user identifier depending on the method the user has been authenticated through
	 * @param securityContext Security Context
	 * @param connectionRepository Connection Repository
	 * @return Identifier
	 */
	public static String getAuthAs(SecurityContext securityContext, ConnectionRepository connectionRepository){
		if (SecurityContextUtils.getAuthThrough(securityContext, connectionRepository) != null) return (String) securityContext.getAuthentication().getPrincipal();
		else return null;
	}
}
