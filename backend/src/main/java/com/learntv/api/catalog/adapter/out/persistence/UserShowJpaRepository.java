package com.learntv.api.catalog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserShowJpaRepository extends JpaRepository<UserShowJpaEntity, UUID> {

    List<UserShowJpaEntity> findByUserId(UUID userId);

    boolean existsByUserIdAndShowId(UUID userId, UUID showId);
}
