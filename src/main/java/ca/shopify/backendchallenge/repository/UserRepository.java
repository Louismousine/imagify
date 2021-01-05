package ca.shopify.backendchallenge.repository;
import ca.shopify.backendchallenge.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author Louis
 *
 */
@RepositoryRestResource(collectionResourceRel = "user_data", path = "user_data")
public interface UserRepository extends CrudRepository<User, String> {

    User findUserByUserName(String name);

    User findUserByApiToken(String token);

    void deleteAll();

}
