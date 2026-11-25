package com.learn.knowledge.exception;

public class UnsupportedFileTypeException extends RuntimeException {
    public UnsupportedFileTypeException(String fileName) {
        super("Unsupported file type: " + fileName + ". Allowed: .pdf, .docx, .txt, .md, .xlsx, .xls, .csv");
    }
}