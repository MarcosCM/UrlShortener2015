package urlshortener2015.heatwave.web;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Jwts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import urlshortener2015.heatwave.entities.ErrorResponse;
import urlshortener2015.heatwave.entities.JsonResponse;
import urlshortener2015.heatwave.entities.SuccessResponse;
import urlshortener2015.heatwave.entities.User;
import urlshortener2015.heatwave.repository.UserRepository;
import urlshortener2015.heatwave.utils.SecurityUtils;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Controller used for user login.
 * When a user wants to log-in, they pass an object with its username and password.
 * If correct, it returns a new Token, that has to be used on every request of the client
 * as an "Authorization bearer" header.
 */
@Controller
@RequestMapping("/user")
public class UsersController {
	
	@Value("${jwt.secret_key}")
    private String key;

    @Autowired
    private UserRepository userRepository;

    public UsersController(){}
    
    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<? extends JsonResponse> login(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam("username") String username, @RequestParam("password") String password)
            throws ServletException {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse("Please, provide both user and password");
            return new ResponseEntity<>(errorResponse,HttpStatus.UNAUTHORIZED);
        }
        else{
            User requestedUser = userRepository.findByUsername(username);
            String hashedPw = SecurityUtils.hashSHA512(password);
            if(requestedUser!=null && requestedUser.getPassword().equals(hashedPw)){
                // Expiration time of token
                Date expirationDate = new Date();
                long expirationTimeInSeconds = 360000;
                expirationDate.setTime(System.currentTimeMillis() + expirationTimeInSeconds * 1000);

                // Create token
                Cookie cookie = new Cookie("JWTAuth",Jwts.builder().setSubject(username)
                        .setIssuedAt(new Date()).setExpiration(expirationDate)
                        .signWith(SignatureAlgorithm.HS512, key).compact());
                response.addCookie(cookie);
                return new ResponseEntity<>(new SuccessResponse<>("OK"), HttpStatus.OK);
            }
            else{
                ErrorResponse errorResponse = new ErrorResponse("User or password incorrect");
                return new ResponseEntity<>(errorResponse,HttpStatus.UNAUTHORIZED);
            }
        }
    }
    
    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<? extends JsonResponse> addUser(@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "password", required = true) String password) {
		User user = new User(username, SecurityUtils.hashSHA512(password));
		if (userRepository.findByUsername(username) == null){
			userRepository.insert(user);
			return new ResponseEntity<>(new SuccessResponse<>("Created"), HttpStatus.CREATED);
		}
		else{
			return new ResponseEntity<>(new ErrorResponse("Conflict"), HttpStatus.CONFLICT);
		} 
	}
}