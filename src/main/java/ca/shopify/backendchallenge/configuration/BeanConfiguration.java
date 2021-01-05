package ca.shopify.backendchallenge.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
/**
 * Class that configures the beans used throughout the application.
 * 
 * @author louis
 *
 */
public class BeanConfiguration implements WebMvcConfigurer {

	/**
	 * Specify Bcrypt as the PW encoder
	 * 
	 * @return
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
