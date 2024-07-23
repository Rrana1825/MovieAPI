package com.Movieflix.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface FileService {
    //Uploading the File
    String uploadFile(String path , MultipartFile file)throws IOException;

    //Converting File to the bytes
    InputStream getResourceFile(String path , String filename) throws FileNotFoundException;

}
