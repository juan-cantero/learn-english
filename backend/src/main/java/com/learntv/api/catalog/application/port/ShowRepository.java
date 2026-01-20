package com.learntv.api.catalog.application.port;

import com.learntv.api.catalog.domain.model.Show;

import java.util.List;
import java.util.Optional;

public interface ShowRepository {

    List<Show> findAll();

    Optional<Show> findBySlug(String slug);

    Show save(Show show);
}
