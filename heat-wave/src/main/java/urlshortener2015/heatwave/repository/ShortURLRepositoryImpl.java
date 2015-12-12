package urlshortener2015.heatwave.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import urlshortener2015.heatwave.entities.ShortURL;

@Repository
public class ShortURLRepositoryImpl implements ShortURLRepositoryCustom {

	private static final Logger logger = LoggerFactory.getLogger(ShortURLRepositoryImpl.class);
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public ShortURL mark(ShortURL su, boolean safe) {
		try {
			su.setSafe(new Boolean(safe));
			mongoTemplate.save(su);
			return su;
		} catch (Exception e) {
			logger.debug("When mark", e);
			return null;
		}
	}
}
