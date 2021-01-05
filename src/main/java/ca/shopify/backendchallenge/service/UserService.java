package ca.shopify.backendchallenge.service;

import ca.shopify.backendchallenge.configuration.JWTTokenProvider;
import ca.shopify.backendchallenge.dto.UserDTO;
import ca.shopify.backendchallenge.exception.TokenException;
import ca.shopify.backendchallenge.exception.RegisterException;
import ca.shopify.backendchallenge.model.User;
import ca.shopify.backendchallenge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * Service to handle login and registration of users.
 */
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JWTTokenProvider jwtTokenProvider;

    /**
     * Creates a user if the input is valid and sends an email to the specified
     * email address.
     *
     * @param user userDTO
     * @return user
     */
    public UserDTO createUser(UserDTO user) throws RegisterException {
        if (user.getPassword() == null || user.getPassword().trim().length() == 0)
            throw new RegisterException("Password can't be null.");
        String validationError = isUserDtoValid(user);
        if (validationError != null)
            throw new RegisterException(validationError);
        if (userRepository.findUserByUserName(user.getUsername()) != null)
            throw new RegisterException("Username is already taken.");
        User user1 = createUserHelper(user);
        userRepository.save(user1);
        return new UserDTO(user1);
    }

    // logs the user in by generating an API token
    public UserDTO login(UserDTO user) throws RegisterException {
        User user1 = userRepository.findUserByUserName(user.getUsername());
        if (user1 == null) {
            throw new RegisterException("Username not found");
        }
        // if the password doesnt match the saved one
        if (!passwordEncoder.matches(user.getPassword(), user1.getPassword())) {
            throw new RegisterException("Incorrect password");
        }
        user1.setApiToken(jwtTokenProvider.createToken(user.getUsername()));
        userRepository.save(user1);
        return new UserDTO(user1);
    }

    /**
     * Method that creates a user object to reduce lines.
     *
     * @param user
     * @return
     */
    private User createUserHelper(UserDTO user) {
        User user1 = new User();
        user1.setPassword(passwordEncoder.encode(user.getPassword()));
        user1.setUserName(user.getUsername());
        String token = jwtTokenProvider.createToken(user1.getUserName());
        user1.setApiToken(token);
        return user1;
    }


    /**
     * Validates the UserDto it is given.
     * Returns null if no error is found. Returns an error message if
     * a violation is found.
     *
     * @param userDto
     * @return
     */
    private String isUserDtoValid(UserDTO userDto) {
        // check if input is valid (username is valid)
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDto);
        for (ConstraintViolation<UserDTO> violation : violations) {
            return violation.getMessage();
        }
        return null;
    }

    /**
     * Method validating an api token and loading the corresponding user from the database
     *
     * @param apiToken: unique api token of the requester
     * @return: the user with the token
     */
    @Transactional
    public User validateApiToken(String apiToken) throws TokenException {

        if (apiToken == null || apiToken.trim().length() < 1) {
            throw new TokenException("Empty API token");
        }

        User c = userRepository.findUserByApiToken(apiToken);
        if (c == null) {
            throw new TokenException("No user has this token");

        }
        if(!jwtTokenProvider.validateToken(apiToken)){
            throw new TokenException("Expired token, please login again");
        }
        return c;
    }
}
