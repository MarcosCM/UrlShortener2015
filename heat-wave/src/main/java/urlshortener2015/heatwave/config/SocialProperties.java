package urlshortener2015.heatwave.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Base {@link ConfigurationProperties properties} for spring social.
 *
 * @author Stephane Nicoll
 * @since 1.2.0
 */
abstract class SocialProperties {

	/**
	 * Application id.
	 */
	private String appId;

	/**
	 * Application secret.
	 */
	private String appSecret;

	public String getAppId() {
		return this.appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppSecret() {
		return this.appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}


}
