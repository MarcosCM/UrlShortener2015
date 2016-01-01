package urlshortener2015.heatwave.repository;

import urlshortener2015.heatwave.entities.ShortURL;

public interface ShortURLRepositoryCustom {

	/**
	 * Marks the URL as safe or not safe
	 * @param url URL to mark
	 * @param safe Safeness
	 * @return Marked URL
	 */
	ShortURL mark(ShortURL url, boolean safe);
}
