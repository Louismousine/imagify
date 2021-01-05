package ca.shopify.backendchallenge.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

//exception when logging in: wrong pw, username not found, account not verified
@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class TokenException extends Exception {
	public TokenException(String msg) {
		super(msg);
	}
}
