package com.learntv.api.catalog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShowJpaRepository extends JpaRepository<ShowJpaEntity, UUID> {

    Optional<ShowJpaEntity> findBySlug(String slug);
}
