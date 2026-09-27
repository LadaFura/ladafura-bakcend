package com.pharmacopee.ladafura.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class FirebaseAuthenticationException extends RuntimeException {

    public FirebaseAuthenticationException(String message) {
        super(message);
    }

    public FirebaseAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
