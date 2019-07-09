package com.moonassist.exception;

public class ConflictException extends RuntimeException {

    public ConflictException(String message, Exception e){
        super(message, e);
    }

}