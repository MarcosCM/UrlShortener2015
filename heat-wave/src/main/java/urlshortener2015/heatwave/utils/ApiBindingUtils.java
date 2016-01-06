package urlshortener2015.heatwave.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.google.api.Google;
import org.springframework.social.twitter.api.Twitter;

public class ApiBindingUtils {
	
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
	 * Gets the API that the user has been authenticated through
	 * @return API id
	 */
	public static String getAuthThrough(ConnectionRepository connectionRepository){
		if (SecurityContextHolder.getContext().getAuthentication() != null){
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
	 * @return Identifier
	 */
	public static String getAuthAs(ConnectionRepository connectionRepository){
		if (SecurityContextHolder.getContext().getAuthentication() != null){
			if (!connectionRepository.findConnections(Twitter.class).isEmpty()){
				return ApiBindingUtils.getTwitterUserName((Twitter) connectionRepository.getPrimaryConnection(Twitter.class).getApi());
			}
			else if (!connectionRepository.findConnections(Facebook.class).isEmpty()){
				return ApiBindingUtils.getFacebookEmail((Facebook) connectionRepository.getPrimaryConnection(Facebook.class).getApi());
			}
			else if (!connectionRepository.findConnections(Google.class).isEmpty()){
				return ApiBindingUtils.getGoogleEmail((Google) connectionRepository.getPrimaryConnection(Google.class).getApi());
			}
		}
		return null;
	}
}
