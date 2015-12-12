package urlshortener2015.heatwave.entities;

import java.net.URI;
import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "urls")
public class ShortURL {

	private String hash;
	private String target;
	private URI uri;
	private Date date;
	private Integer mode;
	private Boolean safe;

	public ShortURL(String hash, String target, URI uri, Date date, Integer mode, Boolean safe) {
		this.hash = hash;
		this.target = target;
		this.uri = uri;
		this.date = date;
		this.mode = mode;
		this.safe = safe;
	}

	public ShortURL() {
	}

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

	public void setSafe(Boolean safe){
		this.safe = safe;
	}
	
}
