package com.learntv.api.generation.application.port.out;

import com.learntv.api.generation.domain.model.Script;

import java.util.Optional;

public interface ScriptCachePort {
    Optional<Script> find(String imdbId, int season, int episode);
    void save(Script script);
}
