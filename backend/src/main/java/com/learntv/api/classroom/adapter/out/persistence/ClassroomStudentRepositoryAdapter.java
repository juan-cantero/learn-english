package com.learntv.api.classroom.adapter.out.persistence;

import com.learntv.api.classroom.application.port.ClassroomStudentRepository;
import com.learntv.api.classroom.domain.model.ClassroomStudent;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ClassroomStudentRepositoryAdapter implements ClassroomStudentRepository {

    private final ClassroomStudentJpaRepository jpaRepository;

    public ClassroomStudentRepositoryAdapter(ClassroomStudentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ClassroomStudent> findByClassroomIdAndStudentId(UUID classroomId, UUID studentId) {
        return jpaRepository.findByClassroomIdAndStudentId(classroomId, studentId)
                .map(ClassroomStudentJpaEntity::toDomain);
    }

    @Override
    public List<ClassroomStudent> findByClassroomId(UUID classroomId) {
        return jpaRepository.findByClassroomId(classroomId).stream()
                .map(ClassroomStudentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<ClassroomStudent> findByStudentId(UUID studentId) {
        return jpaRepository.findByStudentId(studentId).stream()
                .map(ClassroomStudentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public ClassroomStudent save(ClassroomStudent student) {
        ClassroomStudentJpaEntity entity = ClassroomStudentJpaEntity.fromDomain(student);
        ClassroomStudentJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByClassroomIdAndStudentId(UUID classroomId, UUID studentId) {
        return jpaRepository.existsByClassroomIdAndStudentId(classroomId, studentId);
    }
}
