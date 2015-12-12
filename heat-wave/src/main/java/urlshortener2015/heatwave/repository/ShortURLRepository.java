package urlshortener2015.heatwave.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import urlshortener2015.heatwave.entities.ShortURL;

public interface ShortURLRepository extends MongoRepository<ShortURL, String>, ShortURLRepositoryCustom {

	ShortURL findByHash(String hash);

	List<ShortURL> findByTarget(String target);
}
