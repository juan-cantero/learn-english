package com.learntv.api.generation.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for GenerationJob entities.
 */
@Repository
public interface GenerationJobJpaRepository extends JpaRepository<GenerationJobJpaEntity, UUID> {
}
