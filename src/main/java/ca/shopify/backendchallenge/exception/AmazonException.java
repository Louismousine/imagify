package ca.shopify.backendchallenge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AmazonException extends Exception{
    public AmazonException(String msg) {
        super(msg);
    }
}
