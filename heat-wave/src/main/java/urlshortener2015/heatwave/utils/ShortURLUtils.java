package urlshortener2015.heatwave.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.twitter.api.Twitter;

import urlshortener2015.heatwave.entities.ShortURL;

public class ShortURLUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ShortURLUtils.class);
	
	/**
	 * Checks whether the user is in the Facebook list
	 * @param shortURL URL whose list the user is going to be checked
	 * @param facebook Spring Social Facebook object
	 * @return True if the user is in the list, otherwise false
	 */
	public static boolean isUserInList(ShortURL shortURL, Facebook facebook){
		List<String> list = shortURL.getUsers() == null ? null : shortURL.getUsers().get("facebook");
		if (list != null){
			String userMail = facebook.userOperations().getUserProfile().getEmail();
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
	public static boolean isUserInList(ShortURL shortURL, Twitter twitter){
		List<String> list = shortURL.getUsers() == null ? null : shortURL.getUsers().get("twitter");
		if (list != null){
			Pattern p = Pattern.compile("http://twitter\\.com/(.*)");
			Matcher m = p.matcher(twitter.userOperations().getUserProfile().getProfileUrl());
			if (m.find()){
				String userName = m.group(1);
				for(String s : list){
					if (s.equalsIgnoreCase(userName)){
						return true;
					}
				}
			}
		}
		return false;
	}
}