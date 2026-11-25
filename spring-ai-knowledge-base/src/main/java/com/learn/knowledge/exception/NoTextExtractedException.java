package com.learn.knowledge.exception;

public class NoTextExtractedException extends RuntimeException {
    public NoTextExtractedException(String fileName) {
        super("No text could be extracted from " + fileName
                + ". If it is a scanned PDF (an image of text), it needs OCR first.");
    }
}