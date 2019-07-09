package com.moonassist.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message, Exception e){
        super(message, e);
    }

    public NotFoundException(String message){
        super(message);
    }


    public static void check(boolean condition, String message) {
        if (!condition) {
            throw new NotFoundException(message);
        }
    }

}