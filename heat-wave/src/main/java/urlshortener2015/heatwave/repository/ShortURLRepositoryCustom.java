package urlshortener2015.heatwave.repository;

import urlshortener2015.heatwave.entities.ShortURL;

public interface ShortURLRepositoryCustom {

	ShortURL mark(ShortURL urlSafe, boolean safe);
}
