package urlshortener2015.heatwave.utils;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.twitter.api.Twitter;

import urlshortener2015.heatwave.entities.ShortURL;

public class ShortURLUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ShortURLUtils.class);
	
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
	
	public static boolean isUserInList(ShortURL shortURL, Twitter twitter){
		List<String> list = shortURL.getUsers() == null ? null : shortURL.getUsers().get("twitter");
		if (list != null){
			logger.info("Twitter profile URL: " + twitter.userOperations().getUserProfile().getProfileUrl());
			Pattern p = Pattern.compile("(?:http|https)://(?:www?).twitter.com/(.*)");
			String userMail = p.matcher(twitter.userOperations().getUserProfile().getProfileUrl()).group(1);
			for(String s : list){
				if (s.equals(userMail)) return true;
			}
		}
		return false;
	}
}