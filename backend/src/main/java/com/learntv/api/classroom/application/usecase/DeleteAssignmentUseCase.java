package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.AssignmentRepository;
import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.domain.exception.AssignmentNotFoundException;
import com.learntv.api.classroom.domain.exception.NotClassroomOwnerException;
import com.learntv.api.classroom.domain.model.Assignment;
import com.learntv.api.classroom.domain.model.Classroom;

import java.util.UUID;

public class DeleteAssignmentUseCase {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;

    public DeleteAssignmentUseCase(AssignmentRepository assignmentRepository,
                                    ClassroomRepository classroomRepository) {
        this.assignmentRepository = assignmentRepository;
        this.classroomRepository = classroomRepository;
    }

    public void execute(UUID teacherId, UUID assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));

        Classroom classroom = classroomRepository.findById(assignment.getClassroomId())
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));

        if (!classroom.isOwnedBy(teacherId)) {
            throw new NotClassroomOwnerException();
        }

        assignmentRepository.delete(assignment.getId());
    }
}
