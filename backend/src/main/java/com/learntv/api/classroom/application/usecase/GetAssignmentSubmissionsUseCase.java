package com.learntv.api.classroom.application.usecase;

import com.learntv.api.classroom.application.port.AssignmentRepository;
import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.application.port.SubmissionRepository;
import com.learntv.api.classroom.domain.exception.AssignmentNotFoundException;
import com.learntv.api.classroom.domain.exception.NotClassroomOwnerException;
import com.learntv.api.classroom.domain.model.Assignment;
import com.learntv.api.classroom.domain.model.AssignmentSubmission;
import com.learntv.api.classroom.domain.model.Classroom;
import com.learntv.api.user.application.port.UserRepository;
import com.learntv.api.user.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GetAssignmentSubmissionsUseCase {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    public GetAssignmentSubmissionsUseCase(AssignmentRepository assignmentRepository,
                                            ClassroomRepository classroomRepository,
                                            SubmissionRepository submissionRepository,
                                            UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.classroomRepository = classroomRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    public List<SubmissionWithStudent> execute(UUID teacherId, UUID assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));

        Classroom classroom = classroomRepository.findById(assignment.getClassroomId())
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));

        if (!classroom.isOwnedBy(teacherId)) {
            throw new NotClassroomOwnerException();
        }

        List<AssignmentSubmission> submissions = submissionRepository.findByAssignmentId(assignmentId);

        return submissions.stream()
                .map(submission -> {
                    Optional<User> userOpt = userRepository.findById(submission.getStudentId());
                    String studentName = userOpt
                            .map(u -> u.getDisplayName() != null ? u.getDisplayName() : u.getEmail())
                            .orElse("Unknown");
                    String studentEmail = userOpt.map(User::getEmail).orElse("");

                    return new SubmissionWithStudent(submission, studentName, studentEmail);
                })
                .toList();
    }

    public record SubmissionWithStudent(
            AssignmentSubmission submission,
            String studentName,
            String studentEmail
    ) {}
}
