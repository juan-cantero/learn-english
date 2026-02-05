package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.AssignmentRepository;
import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.application.port.SubmissionRepository;
import com.learntv.api.classroom.domain.exception.ClassroomNotFoundException;
import com.learntv.api.classroom.domain.model.Assignment;
import com.learntv.api.classroom.domain.model.AssignmentSubmission;

import java.util.List;
import java.util.UUID;

public class GetClassroomAssignmentsUseCase {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final SubmissionRepository submissionRepository;

    public GetClassroomAssignmentsUseCase(AssignmentRepository assignmentRepository,
                                           ClassroomRepository classroomRepository,
                                           SubmissionRepository submissionRepository) {
        this.assignmentRepository = assignmentRepository;
        this.classroomRepository = classroomRepository;
        this.submissionRepository = submissionRepository;
    }

    public List<AssignmentWithStats> execute(UUID classroomId) {
        classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ClassroomNotFoundException(classroomId));

        List<Assignment> assignments = assignmentRepository.findByClassroomId(classroomId);

        return assignments.stream()
                .map(assignment -> {
                    List<AssignmentSubmission> submissions =
                            submissionRepository.findByAssignmentId(assignment.getId().value());

                    int totalSubmissions = submissions.size();
                    int completedCount = (int) submissions.stream()
                            .filter(AssignmentSubmission::isCompleted)
                            .count();

                    return new AssignmentWithStats(assignment, totalSubmissions, completedCount);
                })
                .toList();
    }

    public record AssignmentWithStats(
            Assignment assignment,
            int totalSubmissions,
            int completedSubmissions
    ) {}
}
