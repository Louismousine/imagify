package ca.shopify.backendchallenge.repository;

import ca.shopify.backendchallenge.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
public class PersistenceTests {
    @Autowired
    private UserRepository userRepository;
    
    /**
     * clears the database before every run
     */
    @BeforeEach
    @AfterEach
    public void clearDatabase() {
        userRepository.deleteAll();
    }
    
    /**
     * Creates a new user for the tests
     *
     * @return user with pre-filled values
     */
    public User createUser() {
        String name = "TestUserName";
        String password = "myPassword1!";
        String apiToken = "token112";
        
        User user = new User();
        user.setUserName(name);
        user.setPassword(password);
        user.setApiToken(apiToken);
        
        userRepository.save(user);
        return user;
    }
    
    @Test
    public void testPersistAndLoadUser() {
        //parameters for users
        String name = "TestUserNamee";
        String password = "myPassword1!";
        String apiToken = "token112";
        
        User user = new User();
        
        // sets everything
        user.setUserName(name);
        user.setPassword(password);
        user.setApiToken(apiToken);
        
        userRepository.save(user);
        user = null;
        
        //asserts if everything can be retrieved from database
        user = userRepository.findUserByUserName(name);
        assertNotNull(user);
        assertEquals(name, user.getUserName());
        assertEquals(password, user.getPassword());
        assertEquals(apiToken, user.getApiToken());
    }
    @Test
    public void testDeleteUser() {
        String name = "TestUserNamee!1";
        String password = "myPassword1!";
        String apiToken = "token112";
        
        User user = new User();
        
        // sets everything
        user.setUserName(name);
        user.setPassword(password);
        user.setApiToken(apiToken);
        userRepository.save(user);
        
        User dbUser = userRepository.findUserByUserName(user.getUserName());
        
        //ensure the user is in the database
        assertEquals(user.getUserName(), dbUser.getUserName());
        
        //delete the user by ID
        // userRepository.deleteById(user.getId());
        userRepository.deleteById(user.getUserName());
        //now it should be null
        assertNull(userRepository.findUserByUserName(user.getUserName()));
    }
}
