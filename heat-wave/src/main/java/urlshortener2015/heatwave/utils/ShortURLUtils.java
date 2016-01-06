package urlshortener2015.heatwave.utils;

import java.util.List;

import org.springframework.social.ApiBinding;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.google.api.Google;
import org.springframework.social.twitter.api.Twitter;

import urlshortener2015.heatwave.entities.ShortURL;

public class ShortURLUtils {
	
	/**
	 * Checks whether the user is in the list linked to the ApiBinding
	 * @param shortURL URL whose list the user is going to be checked
	 * @param api Spring Social ApiBinding object
	 * @return True if the user is in the list, otherwise false
	 */
	public static boolean isUserInList(ShortURL shortURL, ApiBinding api){
		if (api instanceof Facebook) return ShortURLUtils.isUserInFacebookList(shortURL, (Facebook) api);
		else if (api instanceof Twitter) return ShortURLUtils.isUserInTwitterList(shortURL, (Twitter) api);
		else if (api instanceof Google) return ShortURLUtils.isUserInGoogleList(shortURL, (Google) api);
		else return false;
	}
	
	/**
	 * Checks whether the user is in the Facebook list
	 * @param shortURL URL whose list the user is going to be checked
	 * @param facebook Spring Social Facebook object
	 * @return True if the user is in the list, otherwise false
	 */
	public static boolean isUserInFacebookList(ShortURL shortURL, Facebook facebook){
		List<String> list = shortURL.getUsers() == null ? null : shortURL.getUsers().get("facebook");
		if (list != null){
			String userMail = ApiBindingUtils.getFacebookEmail(facebook);
			for(String s : list){
				if (s.equals(userMail)) return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks whether the user is in the Twitter list
	 * @param shortURL URL whose list the user is going to be checked
	 * @param twitter Spring Social Twitter object
	 * @return True if the user is in the list, otherwise false
	 */
	public static boolean isUserInTwitterList(ShortURL shortURL, Twitter twitter){
		List<String> list = shortURL.getUsers() == null ? null : shortURL.getUsers().get("twitter");
		if (list != null){
			String userName = ApiBindingUtils.getTwitterUserName(twitter);
			if (userName != null){
				for(String s : list){
					if (s.equalsIgnoreCase(userName)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks whether the user is in the Google list
	 * @param shortURL URL whose list the user is going to be checked
	 * @param Google Spring Social Google object
	 * @return True if the user is in the list, otherwise false
	 */
	public static boolean isUserInGoogleList(ShortURL shortURL, Google google){
		List<String> list = shortURL.getUsers() == null ? null : shortURL.getUsers().get("google");
		if (list != null){
			String userMail = ApiBindingUtils.getGoogleEmail(google);
			for(String s : list){
				if (s.equals(userMail)) return true;
			}
		}
		return false;
	}
}