package urlshortener2015.heatwave.entities;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "urls")
public class ShortURL {

	private String hash;
	private String target;
	private URI uri;
	private Date date;
	private Integer mode;
	private Boolean safe;
	private Boolean ads;
	private Map<String, List<String>> users;

	public ShortURL(String hash, String target, URI uri, Date date, Integer mode, Boolean safe, Boolean ads, Map<String, List<String>> users) {
		this.hash = hash;
		this.target = target;
		this.uri = uri;
		this.date = date;
		this.mode = mode;
		this.safe = safe;
		this.ads = ads;
		this.users = users;
	}

	public ShortURL() {}

	public String getHash() {
		return hash;
	}

	public String getTarget() {
		return target;
	}

	public URI getUri() {
		return uri;
	}

	public Date getDate() {
		return date;
	}

	public Integer getMode() {
		return mode;
	}

	public Boolean getSafe() {
		return safe;
	}
	
	public Boolean getAds() {
		return ads;
	}
	
	public Map<String, List<String>> getUsers(){
		return users;
	}

	public void setSafe(Boolean safe){
		this.safe = safe;
	}
	
}
