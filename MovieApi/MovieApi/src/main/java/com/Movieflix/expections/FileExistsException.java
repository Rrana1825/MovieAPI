package com.Movieflix.expections;

public class FileExistsException extends RuntimeException{

    public FileExistsException(String message){
        super(message);
    }
}
