package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.AssignmentRepository;
import com.learntv.api.classroom.application.port.ClassroomStudentRepository;
import com.learntv.api.classroom.application.port.SubmissionRepository;
import com.learntv.api.classroom.domain.model.Assignment;
import com.learntv.api.classroom.domain.model.AssignmentSubmission;
import com.learntv.api.classroom.domain.model.ClassroomStudent;
import com.learntv.api.classroom.domain.model.SubmissionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GetStudentAssignmentsUseCase {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final SubmissionRepository submissionRepository;

    public GetStudentAssignmentsUseCase(AssignmentRepository assignmentRepository,
                                         ClassroomStudentRepository classroomStudentRepository,
                                         SubmissionRepository submissionRepository) {
        this.assignmentRepository = assignmentRepository;
        this.classroomStudentRepository = classroomStudentRepository;
        this.submissionRepository = submissionRepository;
    }

    public List<StudentAssignment> execute(UUID studentId) {
        // Get all classrooms the student is enrolled in
        List<ClassroomStudent> memberships = classroomStudentRepository.findByStudentId(studentId);

        return memberships.stream()
                .filter(ClassroomStudent::isActive)
                .flatMap(membership -> {
                    List<Assignment> assignments = assignmentRepository.findByClassroomId(membership.getClassroomId());
                    return assignments.stream()
                            .map(assignment -> {
                                Optional<AssignmentSubmission> submissionOpt = submissionRepository
                                        .findByAssignmentIdAndStudentId(assignment.getId().value(), studentId);

                                SubmissionStatus status = submissionOpt
                                        .map(AssignmentSubmission::getStatus)
                                        .orElse(SubmissionStatus.NOT_STARTED);

                                Integer score = submissionOpt
                                        .map(AssignmentSubmission::getScore)
                                        .orElse(null);

                                return new StudentAssignment(
                                        assignment,
                                        membership.getClassroomId(),
                                        status,
                                        score,
                                        assignment.isOverdue()
                                );
                            });
                })
                .toList();
    }

    public record StudentAssignment(
            Assignment assignment,
            UUID classroomId,
            SubmissionStatus status,
            Integer score,
            boolean overdue
    ) {}
}
