package urlshortener2015.heatwave.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import urlshortener2015.heatwave.entities.Click;

public interface ClickRepository extends MongoRepository<Click, BigInteger>, ClickRepositoryCustom{

	List<Click> findByHash(String hash);
	
	Long countByHash(String hash);
}
