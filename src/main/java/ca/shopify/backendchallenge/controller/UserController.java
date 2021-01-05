package ca.shopify.backendchallenge.controller;

import ca.shopify.backendchallenge.dto.UserDTO;
import ca.shopify.backendchallenge.model.User;
import ca.shopify.backendchallenge.repository.UserRepository;
import ca.shopify.backendchallenge.exception.TokenException;
import ca.shopify.backendchallenge.exception.RegisterException;
import ca.shopify.backendchallenge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author louis
 * User controller class - allows for creation of users, login of users.
 */

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/user")
public class UserController {

	// Declaration of needed services.
	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepo;

	/**
	 * Creates a user account. The Request body is a UserDTO aka password
	 * and username are provided. The method also validates if the
	 * username if already in use and if the any of the input is empty.
	 *
	 * @param user UserDTO
	 * @return user's parameter
	 */
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody UserDTO user) {
		try {
			UserDTO createdUser = userService.createUser(user);
			return new ResponseEntity<>(createdUser, HttpStatus.CREATED); // return created HTTP status
		}
		// If the username is already in use
		catch (RegisterException x) {
			return new ResponseEntity<>(x.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Checks if the user can be logged in. Generates a new token on successful login.
	 *
	 * @param userDto user's login credentials
	 * @return check if can login
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody UserDTO userDto) {
		try {
			userDto = userService.login(userDto);
			return new ResponseEntity<>(userDto, HttpStatus.OK);
		} catch (RegisterException ex) {
			return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Returns a specified user's information.
	 *
	 * @param username user's username'
	 * @return error if cant get user
	 */
	@GetMapping("/{username}")
	public ResponseEntity<?> getUser(@PathVariable String username, @RequestHeader String token) {
		// find a user by username
		User user = userRepo.findUserByUserName(username);
		if (user != null) {
			UserDTO userDTO = new UserDTO(user);
			return new ResponseEntity<>(userDTO, HttpStatus.OK);
			// if the user does not exist, 400
		} else {
			return new ResponseEntity<>("This user does not exist", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Deletes the user's token from the database. The user is now seen as logged
	 * out.
	 *
	 * @param token user's token
	 * @return deletes user token
	 */
	@GetMapping("/logout")
	public ResponseEntity<?> logout(@RequestHeader String token) {
		User user = userRepo.findUserByApiToken(token);
		// if the user cannot be found
		if (user == null) {
			return new ResponseEntity<>("Account not found", HttpStatus.BAD_REQUEST);
		} else {
			// delete the Api token on logout
			user.setApiToken(null);
			userRepo.save(user);
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}


}