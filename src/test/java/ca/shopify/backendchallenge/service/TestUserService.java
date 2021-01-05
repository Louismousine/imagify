package ca.shopify.backendchallenge.service;

import ca.shopify.backendchallenge.dto.UserDTO;
import ca.shopify.backendchallenge.exception.RegisterException;
import ca.shopify.backendchallenge.configuration.JWTTokenProvider;
import ca.shopify.backendchallenge.model.User;
import ca.shopify.backendchallenge.repository.UserRepository;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class TestUserService {

    private static final String USER_NAME = "TestPerson";
    private static final String USER_EMAIL = "TestPerson@email.com";
    private static final String USER_PASSWORD = "myP1+abc";

    private static final String USER_NAME2 = "TestPerson2";
    private static final String USER_EMAIL2 = "TestPerson2@email.com";
    private static final String USER_PASSWORD2 = "myP1+abc2";
    private static final String NEW_USER_PASSWORD = "ad@ssa23Asda";
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTTokenProvider jwtTokenProvider;

    @BeforeEach
    public void setMockOutput() {
        MockitoAnnotations.initMocks(this);
        lenient().when(userRepository.findUserByUserName(anyString())).thenAnswer((InvocationOnMock invocation) -> null);

        Answer<?> returnParameterAsAnswer = (InvocationOnMock invocation) -> invocation.getArgument(0);
        lenient().when(userRepository.save(any(User.class))).thenAnswer(returnParameterAsAnswer);
        lenient().when(userRepository.findUserByApiToken(anyString().toString())).thenAnswer(
                (InvocationOnMock invocation) -> {
                    if (invocation.getArgument(0).equals("VALID")) {
                        return new User();
                    } else {
                        return null;
                    }
                });
    }

    @Test
    public void testCreateUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setPassword(USER_PASSWORD2);
        userDTO.setUsername(USER_NAME2);

        UserDTO createdUser = null;
        try {
            createdUser = userService.createUser(userDTO);
        } catch (RegisterException e) {
            e.printStackTrace();
        }
        assertNotNull(createdUser);
        assertEquals(userDTO.getUsername(), createdUser.getUsername());
    }


    @Test
    public void testCreateWithMissingUsername() {
        UserDTO userDTO = new UserDTO();
        String username = null;
        userDTO.setPassword(USER_PASSWORD);
        try {
            userService.createUser(userDTO);
        } catch (RegisterException e) {
            Assert.assertEquals("must not be null", e.getMessage());
        }
    }

    @Test
    public void testCreateWithMissingPassword() {
        UserDTO userDTO = new UserDTO();

        String password = null;
        userDTO.setPassword(password);
        userDTO.setUsername(USER_NAME);

        try {
            userService.createUser(userDTO);
        } catch (RegisterException e) {
            Assert.assertEquals("Password can't be null.", e.getMessage());
        }
    }

    @Test
    public void testCreateWithEmptyPassword() {
        UserDTO userDTO = new UserDTO();

        String password = "";
        userDTO.setPassword(password);
        userDTO.setUsername(USER_NAME);

        try {
            userService.createUser(userDTO);
        } catch (RegisterException e) {
            Assert.assertEquals("Password can't be null.", e.getMessage());
        }
    }

    @Test
    public void testCreateWithSameUsername() {
        UserDTO user1 = new UserDTO();
        user1.setPassword(USER_PASSWORD);
        user1.setUsername(USER_NAME);

        try {
            userService.createUser(user1);
        } catch (RegisterException ignore) {
            // Assert.fail();
        } // we know that the first one will register for sure

        UserDTO user2 = new UserDTO();
        user2.setUsername(USER_NAME);
        user2.setPassword(USER_PASSWORD);

        try {
            userService.createUser(user2);
        } catch (RegisterException e) {
            Assert.assertEquals("Email is already taken.", e.getMessage());
        }
    }

    @Test
    public void testPasswordShorterThan8() {
        UserDTO userDTO = new UserDTO();

        String password = "1A/c";
        userDTO.setPassword(password);
        userDTO.setUsername(USER_NAME);
        try {
            userService.createUser(userDTO);
        } catch (RegisterException e) {
            Assert.assertEquals("Password must be at least 8 characters in length.", e.getMessage());
        }

    }

    @Test
    public void testPasswordNoNumber() {
        UserDTO userDTO = new UserDTO();

        String password = "/aAasdfg";
        userDTO.setPassword(password);
        userDTO.setUsername(USER_NAME);

        try {
            userService.createUser(userDTO);
        } catch (RegisterException e) {
            Assert.assertEquals("Password must contain at least 1 digit characters.", e.getMessage());
        }

    }

    @Test
    public void testPasswordNoUpperCase() {
        UserDTO userDTO = new UserDTO();

        String password = "/1abcdasdasdasdqdqw";
        userDTO.setPassword(password);
        userDTO.setUsername(USER_NAME);

        try {
            userService.createUser(userDTO);
        } catch (RegisterException e) {
            Assert.assertEquals("Password must contain at least 1 uppercase characters.", e.getMessage());
        }
    }

    @Test
    public void testPasswordNoSpecialCharacter() {
        UserDTO userDTO = new UserDTO();

        String password = "ACSW1abcd";
        userDTO.setPassword(password);
        userDTO.setUsername(USER_NAME);

        try {
            userService.createUser(userDTO);
        } catch (RegisterException e) {
            Assert.assertEquals("Password must contain at least 1 special characters.", e.getMessage());
        }
    }

    /* VALIDATE TOKEN TESTS */

    @Test
    public void validateApiTokenSuccess() {
        userService.validateApiToken("VALID");
    }

    @Test
    public void validateApiTokenEmptyToken() {
        try {
            userService.validateApiToken("");

        } catch (IllegalArgumentException e) {
            assertEquals("Empty API token", e.getMessage());
        }
    }

    @Test
    public void validateApiTokenNullToken() {
        try {
            userService.validateApiToken(null);

        } catch (IllegalArgumentException e) {
            assertEquals("Empty API token", e.getMessage());
        }
    }

    @Test
    public void validateApiTokenNoPoiserMatch() {
        try {
            userService.validateApiToken("NOMATCH");
        } catch (IllegalArgumentException e) {
            assertEquals("No user has this token", e.getMessage());
        }
    }

}
