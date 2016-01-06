package urlshortener2015.heatwave.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import urlshortener2015.heatwave.entities.User;

@Repository
public interface UserRepository extends MongoRepository<User, BigInteger>  {
	
	User findByUsername(String username);
	
	User save(User u);
	
	void deleteById(BigInteger id);
	
	void deleteByUsername(String username);
	
	List<User> findAll();
}