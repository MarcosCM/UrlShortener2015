package urlshortener2015.heatwave.web;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.GenericTypeResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.support.OAuth1ConnectionFactory;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.connect.web.ConnectController;
import org.springframework.social.connect.web.ConnectInterceptor;
import org.springframework.social.connect.web.ConnectSupport;
import org.springframework.social.connect.web.HttpSessionSessionStrategy;
import org.springframework.social.connect.web.SessionStrategy;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.google.api.Google;
import org.springframework.social.security.SocialAuthenticationToken;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.view.RedirectView;

/*
 * Overrides default ConnectController
 */
@Controller
@Scope("session")
@RequestMapping("/connect")
public class CustomConnectController extends ConnectController {

	private static final Logger logger = LoggerFactory.getLogger(CustomConnectController.class);

	@Autowired
	private Facebook facebook;

	@Autowired
	private Twitter twitter;

	@Autowired
	private Google google;

	private ConnectionRepository connectionRepository;

	private ConnectionFactoryLocator connectionFactoryLocator;

	private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();

	private final MultiValueMap<Class<?>, ConnectInterceptor<?>> connectInterceptors = new LinkedMultiValueMap<Class<?>, ConnectInterceptor<?>>();

	private ConnectSupport connectSupport;

	@Inject
	public CustomConnectController(ConnectionFactoryLocator connectionFactoryLocator,
			ConnectionRepository connectionRepository) {
		super(connectionFactoryLocator, connectionRepository);
		this.connectionFactoryLocator = connectionFactoryLocator;
		this.connectionRepository = connectionRepository;
	}
	
	@Override
	@RequestMapping(value="/{providerId}", method=RequestMethod.POST)
	public RedirectView connect(@PathVariable String providerId, NativeWebRequest request) {
		return super.connect(providerId, request);
	}

	@Override
	@RequestMapping(value = "/{providerId}", method = RequestMethod.GET, params = "oauth_token")
	public RedirectView oauth1Callback(@PathVariable String providerId, NativeWebRequest request) {
		connectSupport = new ConnectSupport(sessionStrategy);
		try {
			OAuth1ConnectionFactory<?> connectionFactory = (OAuth1ConnectionFactory<?>) connectionFactoryLocator
					.getConnectionFactory(providerId);
			Connection<?> connection = connectSupport.completeConnection(connectionFactory, request);
			Serializable userProfile = null;
			switch (providerId) {
				case "google":
					google = (Google) connection.getApi();
					userProfile = google.plusOperations().getGoogleProfile().toString();
					break;
				case "facebook":
					facebook = (Facebook) connection.getApi();
					userProfile = facebook.userOperations().getUserProfile();
					break;
				case "twitter":
					twitter = (Twitter) connection.getApi();
					userProfile = twitter.userOperations().getUserProfile();
					break;
				default:
					break;
			}
			SecurityContextHolder.getContext().setAuthentication(new SocialAuthenticationToken(connection, userProfile, null, null));
			addConnection(connection, connectionFactory, request);
		} catch (Exception e) {
			sessionStrategy.setAttribute(request, PROVIDER_ERROR_ATTRIBUTE, e);
			logger.warn("Exception while handling OAuth1 callback (" + e.getMessage() + "). Redirecting to "
					+ providerId + " connection status page.");
		}
		return connectionStatusRedirect(providerId, request);
	}

	@Override
	@RequestMapping(value = "/{providerId}", method = RequestMethod.GET, params = "code")
	public RedirectView oauth2Callback(@PathVariable String providerId, NativeWebRequest request) {
		connectSupport = new ConnectSupport(sessionStrategy);
		try {
			OAuth2ConnectionFactory<?> connectionFactory = (OAuth2ConnectionFactory<?>) connectionFactoryLocator
					.getConnectionFactory(providerId);
			Connection<?> connection = connectSupport.completeConnection(connectionFactory, request);
			Serializable userProfile = null;
			switch (providerId) {
				case "google":
					google = (Google) connection.getApi();
					userProfile = google.plusOperations().getGoogleProfile().toString();
					break;
				case "facebook":
					facebook = (Facebook) connection.getApi();
					userProfile = facebook.userOperations().getUserProfile();
					break;
				case "twitter":
					twitter = (Twitter) connection.getApi();
					userProfile = twitter.userOperations().getUserProfile();
					break;
				default:
					break;
			}
			SecurityContextHolder.getContext()
					.setAuthentication(new SocialAuthenticationToken(connection, userProfile, null, null));
			addConnection(connection, connectionFactory, request);
		} catch (Exception e) {
			sessionStrategy.setAttribute(request, PROVIDER_ERROR_ATTRIBUTE, e);
			logger.warn("Exception while handling OAuth2 callback (" + e.getMessage() + "). Redirecting to "
					+ providerId + " connection status page.");
		}
		return connectionStatusRedirect(providerId, request);
	}

	private void addConnection(Connection<?> connection, ConnectionFactory<?> connectionFactory, WebRequest request) {
		try {
			connectionRepository.addConnection(connection);
			postConnect(connectionFactory, connection, request);
		} catch (Exception e) {
			sessionStrategy.setAttribute(request, DUPLICATE_CONNECTION_ATTRIBUTE, e);
		}
	}

	private void postConnect(ConnectionFactory<?> connectionFactory, Connection<?> connection, WebRequest request) {
		for (ConnectInterceptor interceptor : interceptingConnectionsTo(connectionFactory)) {
			interceptor.postConnect(connection, request);
		}
	}

	private List<ConnectInterceptor<?>> interceptingConnectionsTo(ConnectionFactory<?> connectionFactory) {
		Class<?> serviceType = GenericTypeResolver.resolveTypeArgument(connectionFactory.getClass(),
				ConnectionFactory.class);
		List<ConnectInterceptor<?>> typedInterceptors = connectInterceptors.get(serviceType);
		if (typedInterceptors == null) {
			typedInterceptors = Collections.emptyList();
		}
		return typedInterceptors;
	}

	@Override
	protected String connectedView(String providerId) {
		logger.info("Redirecting to " + providerId + " view");
		// User will be redirected to home, no matter the provider
		return "redirect:/";
	}
}
