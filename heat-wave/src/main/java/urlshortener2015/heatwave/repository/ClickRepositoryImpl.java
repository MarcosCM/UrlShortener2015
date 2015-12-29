package urlshortener2015.heatwave.repository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ClickRepositoryImpl implements ClickRepositoryCustom {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public Map<String, Map<String, Integer>> aggregateInfoByHash(String hash) {
		return null;
	}
	
}
