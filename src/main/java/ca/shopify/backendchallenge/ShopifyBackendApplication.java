package ca.shopify.backendchallenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entry point of the application.
 * @author louis
 *
 */
@RestController
@SpringBootApplication
@EnableJpaRepositories
public class ShopifyBackendApplication {

	/**
	 * Main method; launches the application.
	 * @param args
	 */
  public static void main(String[] args) {
    SpringApplication.run(ShopifyBackendApplication.class, args);
  }

}
