package urlshortener2015.heatwave.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.social.ApiBinding;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.google.api.Google;
import org.springframework.social.twitter.api.Twitter;

public class ApiBindingUtils {
	
	/**
	 * Gets the identifier for a specific API
	 * @param api Spring Social API
	 * @return String Identifier
	 */
	public static String getId(ApiBinding api){
		if (api instanceof Facebook) return ApiBindingUtils.getFacebookEmail((Facebook) api);
		else if (api instanceof Twitter) return ApiBindingUtils.getTwitterUserName((Twitter) api);
		else if (api instanceof Google) return ApiBindingUtils.getGoogleEmail((Google) api);
		else return null;
	}
	
	/**
	 * Gets the Facebook user email
	 * @param facebook Spring Social Facebook object
	 * @return User email
	 */
	public static String getFacebookEmail(Facebook facebook){
		return facebook.userOperations().getUserProfile().getEmail();
	}
	
	/**
	 * Gets the Twitter user email
	 * @param twitter Spring Social Twitter object
	 * @return User email
	 */
	public static String getTwitterUserName(Twitter twitter){
		Pattern p = Pattern.compile("http://twitter\\.com/(.*)");
		Matcher m = p.matcher(twitter.userOperations().getUserProfile().getProfileUrl());
		String userName = null;
		if (m.find()){
			userName = m.group(1);
		}
		return userName;
	}
	
	/**
	 * Gets the Google user email
	 * @param google Spring Social Google object
	 * @return User email
	 */
	public static String getGoogleEmail(Google google){
		return google.plusOperations().getGoogleProfile().getAccountEmail();
	}
	
	/**
	 * Gets the API name that the user has been authenticated through
	 * @param securityContext Security Context
	 * @param connectionRepository Connection Repository
	 * @return API name if authed with a social API, otherwise null
	 */
	public static String getAuthThrough(SecurityContext securityContext, ConnectionRepository connectionRepository){
		if (securityContext.getAuthentication() != null){
			if (!connectionRepository.findConnections(Twitter.class).isEmpty()){
				return "twitter";
			}
			else if (!connectionRepository.findConnections(Facebook.class).isEmpty()){
				return "facebook";
			}
			else if (!connectionRepository.findConnections(Google.class).isEmpty()){
				return "google";
			}
		}
		return null;
	}
	
	/**
	 * Gets a user identifier depending on the API that they used to be authenticated
	 * @param securityContext Security Context
	 * @param connectionRepository Connection Repository
	 * @return API identifier if authed with a social API, otherwise null
	 */
	public static String getAuthAs(SecurityContext securityContext, ConnectionRepository connectionRepository){
		switch(ApiBindingUtils.getAuthThrough(securityContext, connectionRepository)){
			case "twitter":
				return ApiBindingUtils.getTwitterUserName((Twitter) connectionRepository.getPrimaryConnection(Twitter.class).getApi());
			case "facebook":
				return ApiBindingUtils.getFacebookEmail((Facebook) connectionRepository.getPrimaryConnection(Facebook.class).getApi());
			case "google":
				return ApiBindingUtils.getGoogleEmail((Google) connectionRepository.getPrimaryConnection(Google.class).getApi());
			default:
				return null;
		}
	}
}
