package ca.shopify.backendchallenge.configuration;


import ca.shopify.backendchallenge.model.User;
import ca.shopify.backendchallenge.repository.UserRepository;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;


/**
 * This class generates the JWTTOkens used to confirm a user's account. Taken from Baeldung's tutorials.
 * @author louis
 *
 */
@Component
public class JWTTokenProvider {
	private String secretKey = "secret";

	@Autowired
	private UserRepository userRepo;

	@PostConstruct
	protected void init() {
		secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
	}

	// creates a token
	public String createToken(String username) {
		Claims claims = Jwts.claims().setSubject(username);
		Date now = new Date();
		//a token is valid for 1 hour
		long validityInMilliseconds = 3600000;
		Date validity = new Date(now.getTime() + validityInMilliseconds);
		return Jwts.builder()//
				.setClaims(claims)//
				.setIssuedAt(now)//
				.setExpiration(validity)//
				.signWith(SignatureAlgorithm.HS256, secretKey)//
				.compact();
	}

	// dont know what this does but it looks important
	public Authentication getAuthentication(String token) {
		User userDetails = this.userRepo.findUserByUserName(getUsername(token));
		return new UsernamePasswordAuthenticationToken(userDetails, "", null);
	}

	//extracts the username from the token
	public String getUsername(String token) {
		return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
	}

	// dont know what this does but it looks important
	public String resolveToken(HttpServletRequest req) {
		String bearerToken = req.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

	//check that the validation was made before the expiry time
	public boolean validateToken(String token) {
		try {
			Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
			return claims.getBody().getExpiration().before(new Date()) || true;
		} catch (JwtException | IllegalArgumentException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
