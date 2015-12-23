package urlshortener2015.heatwave.utils;

import java.util.List;

import org.springframework.social.facebook.api.Facebook;

import urlshortener2015.heatwave.entities.ShortURL;

public class ShortURLUtils {
	
	public static boolean isUserInFacebookList(ShortURL shortURL, Facebook facebook){
		List<String> list = shortURL.getUsers() == null ? null : shortURL.getUsers().get("facebook");
		if (list != null){
			String userMail = facebook.userOperations().getUserProfile().getEmail();
			for(String s : list){
				if (s.equals(userMail)) return true;
			}
		}
		return false;
	}
}