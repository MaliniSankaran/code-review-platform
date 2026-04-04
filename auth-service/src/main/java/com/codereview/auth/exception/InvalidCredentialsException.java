package com.codereview.auth.exception;

//401 Unauthorized
public class InvalidCredentialsException extends RuntimeException
{
    public InvalidCredentialsException(String message)
    {
        super(message);
    }
}
