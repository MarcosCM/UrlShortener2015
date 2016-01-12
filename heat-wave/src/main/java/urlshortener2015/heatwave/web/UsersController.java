package urlshortener2015.heatwave.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

import urlshortener2015.heatwave.entities.User;
import urlshortener2015.heatwave.repository.UserRepository;
import urlshortener2015.heatwave.utils.SecurityUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller used for user login.
 * When a user wants to log-in, they pass an object with its username and password.
 * If correct, it returns a new Token, that has to be used on every request of the client
 * as an "Authorization bearer" header.
 */
@Controller
@RequestMapping("/user")
public class UsersController {

    @Autowired
    private UserRepository userRepository;

    public UsersController(){}
    
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam("username") String username, @RequestParam("password") String password) throws ServletException {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Please, introduce your username and password");
        }
        else{
            User requestedUser = userRepository.findByUsername(username);
            String hashedPw = SecurityUtils.hashSHA512(password);
            if(requestedUser != null && requestedUser.getPassword().equals(hashedPw)){
            	SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(username, password, null));
                return MainController.DEFAULT_HOME_PATH;
            }
            else{
            	throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Incorrect user and password combination");
            }
        }
    }
    
    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = "application/json")
	public String addUser(@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "password", required = true) String password) {
		if (userRepository.findByUsername(username) == null){
			User user = new User(username, SecurityUtils.hashSHA512(password));
			userRepository.insert(user);
			SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(username, password, null));
			return MainController.DEFAULT_HOME_PATH;
		}
		else{
			throw new HttpClientErrorException(HttpStatus.CONFLICT, "User already exists");
		} 
	}
}