package com.learntv.api.classroom.adapter.out.persistence;

import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.domain.model.Classroom;
import com.learntv.api.classroom.domain.model.ClassroomId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ClassroomRepositoryAdapter implements ClassroomRepository {

    private final ClassroomJpaRepository jpaRepository;

    public ClassroomRepositoryAdapter(ClassroomJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Classroom> findById(ClassroomId id) {
        return findById(id.value());
    }

    @Override
    public Optional<Classroom> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(ClassroomJpaEntity::toDomain);
    }

    @Override
    public Optional<Classroom> findByJoinCode(String joinCode) {
        return jpaRepository.findByJoinCode(joinCode)
                .map(ClassroomJpaEntity::toDomain);
    }

    @Override
    public List<Classroom> findByTeacherId(UUID teacherId) {
        return jpaRepository.findByTeacherId(teacherId).stream()
                .map(ClassroomJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Classroom save(Classroom classroom) {
        ClassroomJpaEntity entity = ClassroomJpaEntity.fromDomain(classroom);
        ClassroomJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public void delete(ClassroomId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsByJoinCode(String joinCode) {
        return jpaRepository.existsByJoinCode(joinCode);
    }
}
