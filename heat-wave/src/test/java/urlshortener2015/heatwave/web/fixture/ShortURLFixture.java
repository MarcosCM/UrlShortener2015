package urlshortener2015.heatwave.web.fixture;

import java.util.List;
import java.util.HashMap;

import urlshortener2015.heatwave.entities.*;

public class ShortURLFixture {

	public static ShortURL someUrl() {
		return new ShortURL("someKey", "http://example.com/", null, null, null, true, true, new HashMap<String, List<String>>(), null, null);
	}
}
