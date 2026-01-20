package com.learntv.api.catalog.adapter.out.persistence;

import com.learntv.api.catalog.application.port.ShowRepository;
import com.learntv.api.catalog.domain.model.Show;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ShowRepositoryAdapter implements ShowRepository {

    private final ShowJpaRepository jpaRepository;

    public ShowRepositoryAdapter(ShowJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Show> findAll() {
        return jpaRepository.findAll().stream()
                .map(ShowJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Show> findBySlug(String slug) {
        return jpaRepository.findBySlug(slug)
                .map(ShowJpaEntity::toDomain);
    }

    @Override
    public Show save(Show show) {
        ShowJpaEntity entity = ShowJpaEntity.fromDomain(show);
        ShowJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }
}
