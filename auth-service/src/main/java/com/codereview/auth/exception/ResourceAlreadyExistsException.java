package com.codereview.auth.exception;

//409 Conflict
public class ResourceAlreadyExistsException extends RuntimeException{

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
