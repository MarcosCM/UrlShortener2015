package urlshortener2015.heatwave.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.social.FacebookAutoConfiguration;
import org.springframework.boot.autoconfigure.social.TwitterAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurer;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.security.AuthenticationNameUserIdSource;

import urlshortener2015.heatwave.config.GoogleAutoConfiguration;
import urlshortener2015.heatwave.repository.MongoConnectionTransformers;
import urlshortener2015.heatwave.repository.MongoUsersConnectionRepository;

@Configuration
@EnableSocial
@EnableAutoConfiguration(exclude={GoogleAutoConfiguration.class, TwitterAutoConfiguration.class, FacebookAutoConfiguration.class})

public class SocialConfig implements SocialConfigurer{
	
	@Autowired
	MongoTemplate mongoTemplate;

   

	@Override
	public void addConnectionFactories(ConnectionFactoryConfigurer connectionFactoryConfigurer,
			Environment environment) {
//		connectionFactoryConfigurer.addConnectionFactory(new TwitterConnectionFactory(
//				environment.getProperty("spring.social.twitter.appId"),
//				environment.getProperty("spring.social.twitter.appSecret")));
//		connectionFactoryConfigurer.addConnectionFactory(new FacebookConnectionFactory(
//				environment.getProperty("spring.social.facebook.appId"),
//				environment.getProperty("spring.social.facebook.appSecret")));
//		connectionFactoryConfigurer.addConnectionFactory(new GoogleConnectionFactory(
//				environment.getProperty("spring.social.google.appId"),
//				environment.getProperty("spring.social.google.appSecret")));
		
	}

	@Override
	public UserIdSource getUserIdSource() {
		return new AuthenticationNameUserIdSource();
	}

	@Override
	public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
		return new MongoUsersConnectionRepository(mongoTemplate, connectionFactoryLocator, new MongoConnectionTransformers(connectionFactoryLocator, Encryptors.noOpText()));
	}

}
