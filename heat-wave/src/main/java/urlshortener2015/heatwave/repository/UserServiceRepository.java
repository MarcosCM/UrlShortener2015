package urlshortener2015.heatwave.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public interface UserServiceRepository extends MongoRepository<User, String> {

	public User findUserByUsername(String username) throws UsernameNotFoundException;

	public User save(User user);
}