package com.learntv.api.catalog.domain.exception;

public class ShowNotFoundException extends RuntimeException {

    public ShowNotFoundException(String slug) {
        super("Show not found with slug: " + slug);
    }
}
