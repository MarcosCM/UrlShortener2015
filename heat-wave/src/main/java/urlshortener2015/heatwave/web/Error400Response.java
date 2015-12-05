package urlshortener2015.heatwave.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value=HttpStatus.BAD_REQUEST)  // 404
public class Error400Response extends RuntimeException {

	public Error400Response(String msg) {
		super (msg);
	}
	
}
