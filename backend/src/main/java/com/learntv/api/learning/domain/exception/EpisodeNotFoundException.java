package com.learntv.api.learning.domain.exception;

public class EpisodeNotFoundException extends RuntimeException {

    public EpisodeNotFoundException(String showSlug, String episodeSlug) {
        super("Episode not found: " + showSlug + "/" + episodeSlug);
    }
}
