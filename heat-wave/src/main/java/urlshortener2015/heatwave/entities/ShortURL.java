package urlshortener2015.heatwave.entities;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "urls")
public class ShortURL {
	
	@Id
	private String hash;
	private String target;
	private URI uri;
	private Date date;
	private Integer mode;
	private Boolean safe;
	private Boolean ads;
	private Map<String, List<String>> users;
	private Map<Integer, String> rules;
	private int id;
	
	public ShortURL(String hash, String target, URI uri, Date date, Integer mode, Boolean safe, Boolean ads, Map<String, List<String>> users) {
		this.hash = hash;
		this.target = target;
		this.uri = uri;
		this.date = date;
		this.mode = mode;
		this.safe = safe;
		this.ads = ads;
		this.users = users;
		this.rules = new HashMap<Integer, String>();
		this.id = 1;
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
	
	public Map<Integer, String> getRules(){
		return rules;
	}
	
	public void addRule(String script){
		this.rules.put(id, script);
		id += 1;
	}
	
	public void modifyRule(int id, String script){
		this.rules.put(id, script);
	}
	
	public void deleteRule(int id){
		this.rules.remove(id);
	}
	
	public void setSafe(Boolean safe){
		this.safe = safe;
	}
	
}
