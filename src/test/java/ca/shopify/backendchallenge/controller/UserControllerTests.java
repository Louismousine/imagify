package ca.shopify.backendchallenge.controller;

import ca.shopify.backendchallenge.dto.UserDTO;
import ca.shopify.backendchallenge.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    @AfterEach
    public void clearDatabase() {
        userRepository.deleteAll();
    }

    @Test
    public void testRegisterUserAndLogin() throws Exception {
        UserDTO userDto = new UserDTO();
        userDto.setUsername("test");
        userDto.setPassword("validPassword123!");

        MvcResult mvcResult =
                mvc.perform(
                        post("/api/user/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDto))
                                .characterEncoding("utf-8"))
                        .andExpect(status().isCreated())
                        .andReturn();

        // get object from response
        userDto =
                objectMapper.readValue(
                        mvcResult.getResponse().getContentAsString(), UserDTO.class);

        // test that the user is registered
        assertEquals("username", userDto.getUsername(), "test");

        // test that the user can be loggedin
        userDto.setPassword("validPassword123!");

        mvcResult =
                mvc.perform(
                        post("/api/user/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDto))
                                .characterEncoding("utf-8"))
                        .andExpect(status().isOk())
                        .andReturn();

        // get object from response
        userDto =
                objectMapper.readValue(
                        mvcResult.getResponse().getContentAsString(), UserDTO.class);
        //test that a token is sent back
        assertNotNull("token",userDto.getApiToken());
    }
}