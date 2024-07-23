package com.Movieflix.expections;

public class EmptyFileException extends Throwable {
    public EmptyFileException(String message) {
        super(message);
    }
}
