package com.example.srp.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class Base64ToMultipartUtil implements MultipartFile {

    private final byte[] fileContent;
    private final String fileName;
    private final String contentType;

    public Base64ToMultipartUtil(byte[] fileContent, String fileName, String contentType) {
        this.fileContent = fileContent;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public String getOriginalFilename() {
        return this.fileName;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return fileContent == null || fileContent.length == 0;
    }

    @Override
    public long getSize() {
        return fileContent.length;
    }

    @Override
    public byte[] getBytes() {
        return fileContent;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(fileContent);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        try (FileOutputStream out = new FileOutputStream(dest)) {
            out.write(fileContent);
        }
    }
}

