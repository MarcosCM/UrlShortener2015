package urlshortener2015.heatwave.web.fixture;

import urlshortener2015.heatwave.web.*;

import java.net.URI;
import java.util.Date;

import urlshortener2015.heatwave.entities.*;

public class ShortURLFixture {

	public static ShortURL someUrl() {
		return new ShortURL("someKey", "http://example.com/", null, null, null, true, true, null);
	}
}
