package urlshortener2015.heatwave.utils;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.social.connect.ConnectionRepository;

import urlshortener2015.heatwave.entities.ShortURL;

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
	
	/**
	 * Checks whether the authed user is the creator of the shortened URL or they are anonymous
	 * @param url Shortened URL
	 * @param securityContext Security Context
	 * @param connectionRepository Connection Repository
	 * @return True if the authed user is the creator of the shortened URL or they are anonymous, otherwise false
	 */
	public static boolean isCreator(ShortURL url, SecurityContext securityContext, ConnectionRepository connectionRepository){
		String authAs = SecurityContextUtils.getAuthAs(securityContext, connectionRepository);
		if (authAs.equals("anonymousUser")){
			String authThrough = SecurityContextUtils.getAuthThrough(securityContext, connectionRepository);
			if (url.getCreatorAuthAs() != null && url.getCreatorAuthAs().equals(authAs)
					&& url.getCreatorAuthThrough() != null && url.getCreatorAuthThrough().equals(authThrough)) return true;
			else return false;
		}
		else return false;
	}
}
