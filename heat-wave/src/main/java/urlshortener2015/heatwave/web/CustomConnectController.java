package urlshortener2015.heatwave.web;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.view.RedirectView;

import urlshortener2015.heatwave.utils.ApiBindingUtils;

/**
 * Overrides default ConnectController
 */
@Controller
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
    public CustomConnectController(ConnectionFactoryLocator connectionFactoryLocator, ConnectionRepository connectionRepository) {
        super(connectionFactoryLocator, connectionRepository);
        this.connectionFactoryLocator = connectionFactoryLocator;
        this.connectionRepository = connectionRepository;
    }
    
	/**
	 * Redirects to the home page
	 * @param request Request
	 * @param model Model
	 * @return Home page
	 */
	@RequestMapping(value = "/")
	public String homePage(HttpServletRequest request, Model model){
		logger.info("Home page");
		model.addAttribute("authThrough", this.getAuthThrough());
		model.addAttribute("authAs", this.getAuthAs());
		return MainController.DEFAULT_HOME_PATH;
	}
    
    @Override
	@RequestMapping(value="/connect/{providerId}", method=RequestMethod.GET, params="oauth_token")
	public RedirectView oauth1Callback(@PathVariable String providerId, NativeWebRequest request) {
		connectSupport = new ConnectSupport(sessionStrategy);
		try {
			OAuth1ConnectionFactory<?> connectionFactory = (OAuth1ConnectionFactory<?>) connectionFactoryLocator.getConnectionFactory(providerId);
			Connection<?> connection = connectSupport.completeConnection(connectionFactory, request);
			String uniqueId = "";
			switch(providerId){
				case "google":
					google = (Google) connection.getApi();
					uniqueId = ApiBindingUtils.getGoogleEmail(google);
					break;
				case "facebook":
					facebook = (Facebook) connection.getApi();
					uniqueId = ApiBindingUtils.getFacebookEmail(facebook);
					break;
				case "twitter":
					twitter = (Twitter) connection.getApi();
					uniqueId = ApiBindingUtils.getTwitterUserName(twitter);
					break;
				default:
					break;
			}
			SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(uniqueId, null, null));
			addConnection(connection, connectionFactory, request);
		} catch (Exception e) {
			sessionStrategy.setAttribute(request, PROVIDER_ERROR_ATTRIBUTE, e);
			logger.warn("Exception while handling OAuth1 callback (" + e.getMessage() + "). Redirecting to " + providerId +" connection status page.");
		}
		return connectionStatusRedirect(providerId, request);
	}
    
	@Override
	@RequestMapping(value="/connect/{providerId}", method=RequestMethod.GET, params="code")
	public RedirectView oauth2Callback(@PathVariable String providerId, NativeWebRequest request) {
		connectSupport = new ConnectSupport(sessionStrategy);
		try {
			OAuth2ConnectionFactory<?> connectionFactory = (OAuth2ConnectionFactory<?>) connectionFactoryLocator.getConnectionFactory(providerId);
			Connection<?> connection = connectSupport.completeConnection(connectionFactory, request);
			Serializable userProfile = null;
			switch(providerId){
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
			logger.warn("Exception while handling OAuth2 callback (" + e.getMessage() + "). Redirecting to " + providerId +" connection status page.");
		}
		return connectionStatusRedirect(providerId, request);
	}
	
	/**
	 * Gets the API that the user has been authenticated through
	 * @return API id
	 */
	public String getAuthThrough(){
		if (SecurityContextHolder.getContext().getAuthentication() != null){
			if (!connectionRepository.findConnections(Twitter.class).isEmpty()){
				return "twitter";
			}
			else if (!connectionRepository.findConnections(Facebook.class).isEmpty()){
				return "facebook";
			}
			else if (!connectionRepository.findConnections(Google.class).isEmpty()){
				return "google";
			}
		}
		return null;
	}
	
	/**
	 * Gets a user identifier depending on the API that they used to be authenticated
	 * @return Identifier
	 */
	public String getAuthAs(){
		if (SecurityContextHolder.getContext().getAuthentication() != null){
			if (!connectionRepository.findConnections(Twitter.class).isEmpty()){
				return ApiBindingUtils.getTwitterUserName((Twitter) connectionRepository.getPrimaryConnection(Twitter.class).getApi());
			}
			else if (!connectionRepository.findConnections(Facebook.class).isEmpty()){
				return ApiBindingUtils.getFacebookEmail((Facebook) connectionRepository.getPrimaryConnection(Facebook.class).getApi());
			}
			else if (!connectionRepository.findConnections(Google.class).isEmpty()){
				return ApiBindingUtils.getGoogleEmail((Google) connectionRepository.getPrimaryConnection(Google.class).getApi());
			}
		}
		return null;
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
		Class<?> serviceType = GenericTypeResolver.resolveTypeArgument(connectionFactory.getClass(), ConnectionFactory.class);
		List<ConnectInterceptor<?>> typedInterceptors = connectInterceptors.get(serviceType);
		if (typedInterceptors == null) {
			typedInterceptors = Collections.emptyList();
		}
		return typedInterceptors;
	}
    
    /*@RequestMapping(value = "/{providerId:twitter}/{id:(?!link|!stadistics|index).*}", method = RequestMethod.POST)
    protected RedirectView connectOAuth1(@PathVariable("providerId") String providerId, @PathVariable("id") String id, NativeWebRequest request){
    	logger.info("POST TO /connect/"+providerId+"/"+id);
    	return super.connect(providerId, request);
    }
    
    @RequestMapping(value = "/{providerId:twitter}/{id:(?!link|!stadistics|index).*}", method = RequestMethod.GET, params = {"oauth_token"})
    protected String OAuth1Callback(@PathVariable("providerId") String providerId, @PathVariable("id") String id, NativeWebRequest request, Model model,
    		RedirectAttributes redirectAttributes){
    	// Using redirectAttributes to keep the model attributes in the redirect target model
    	logger.info("GET TO /connect/"+providerId+"/"+id+" (OAuth1Callback)");
    	super.oauth1Callback(providerId, request);
    	return redirecToConnectedView(id, providerId, redirectAttributes);
    }
    
    @RequestMapping(value = "/{providerId:google|facebook}/{id:(?!link|!stadistics|index).*}", method = RequestMethod.POST)
    protected RedirectView connectOAuth2(@PathVariable("providerId") String providerId, NativeWebRequest request){
    	logger.info("POST TO /connect/"+providerId);
    	return super.connect(providerId, request);
    }
    
    @RequestMapping(value = "/{providerId:google|facebook}/{id:(?!link|!stadistics|index).*}", method = RequestMethod.GET, params={"code"})
    protected String OAuth2Callback(@PathVariable("providerId") String providerId, @PathVariable("id") String id, NativeWebRequest request, Model model,
    		RedirectAttributes redirectAttributes){
    	// Using redirectAttributes to keep the model attributes in the redirect target model
    	logger.info("GET TO /connect/"+providerId+"/"+id);
    	super.oauth2Callback(providerId, request);
    	return redirecToConnectedView(id, providerId, redirectAttributes);
    }
    
    @RequestMapping(value = "/{providerId:google|facebook}/{id:(?!link|!stadistics|index).*}", method = RequestMethod.GET, params = {"error"})
    protected String OAuth2Err(@PathVariable("providerId") String providerId, @PathVariable("id") String id, NativeWebRequest request, Model model){
    	logger.info("GET TO /connect/" + providerId + "/" + id + " (OAuth2Err)");
    	return "redirect:/connect/error";
    }*/

    @Override
    protected String connectedView(String providerId) {
    	logger.info("Redirecting to " + providerId + " view");
    	// User will be redirected to home, no matter the provider
        return "redirect:/";
    }
}
