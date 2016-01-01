package urlshortener2015.heatwave.entities;

import java.math.BigInteger;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "clicks")
public class Click {

	@Id
	private BigInteger id;
	private String hash;
	private Date date;
	private String browser;
	private String platform;
	private String ip;
	private String country;

	public Click(BigInteger id, String hash, Date date, String browser, String platform, String ip, String country) {
		this.id = id;
		this.hash = hash;
		this.date = date;
		this.browser = browser;
		this.platform = platform;
		this.ip = ip;
		this.country = country;
	}

	public BigInteger getId() {
		return id;
	}

	public String getHash() {
		return hash;
	}

	public Date getDate() {
		return date;
	}

	public String getBrowser() {
		return browser;
	}

	public String getPlatform() {
		return platform;
	}

	public String getIp() {
		return ip;
	}

	public String getCountry() {
		return country;
	}
}
