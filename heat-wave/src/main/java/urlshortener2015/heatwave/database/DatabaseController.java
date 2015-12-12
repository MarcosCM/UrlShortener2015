package urlshortener2015.heatwave.database;

import java.util.List;

import urlshortener2015.heatwave.entities.ShortURL;

public interface DatabaseController {

	ShortURL findByKey(String id);

	List<ShortURL> findByTarget(String target);

	ShortURL save(ShortURL su);

	ShortURL mark(ShortURL urlSafe, boolean safeness);

	void update(ShortURL su);

	void delete(String id);

	Long count();

	List<ShortURL> list(Long limit, Long offset);
	
}
