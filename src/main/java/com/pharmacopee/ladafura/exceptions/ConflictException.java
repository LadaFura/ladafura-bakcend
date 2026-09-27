package com.pharmacopee.ladafura.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Conflit : %s existe déjà avec %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
