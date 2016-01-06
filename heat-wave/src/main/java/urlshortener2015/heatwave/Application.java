package urlshortener2015.heatwave;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import urlshortener2015.heatwave.entities.URLProtection;
import urlshortener2015.heatwave.filters.WebTokenFilter;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

	@Value("${jwt.secret_key}")
	private String key;
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	@Bean
	public FilterRegistrationBean jwtFilter() {
		final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		WebTokenFilter authenticationFilter = new WebTokenFilter(key);

		//Protect all methods from "/link"
		URLProtection linkURL = new URLProtection("/link.*");
		linkURL.setAllMethods();
		authenticationFilter.addUrlToProtect(linkURL);

		//Protect GET from simple link information
		URLProtection infoURL = new URLProtection("/.*\\+");
		infoURL.addMethod("GET");
		authenticationFilter.addUrlToProtect(infoURL);

		registrationBean.setFilter(authenticationFilter);

		return registrationBean;
	}
}