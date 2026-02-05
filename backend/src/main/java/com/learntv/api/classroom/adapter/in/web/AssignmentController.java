package com.learntv.api.classroom.adapter.in.web;

import com.learntv.api.classroom.application.usecase.*;
import com.learntv.api.classroom.domain.model.Assignment;
import com.learntv.api.classroom.domain.model.AssignmentSubmission;
import com.learntv.api.shared.config.security.AuthenticatedUser;
import com.learntv.api.shared.config.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Assignments", description = "Assignment management operations")
public class AssignmentController {

    private final CreateAssignmentUseCase createAssignmentUseCase;
    private final GetClassroomAssignmentsUseCase getClassroomAssignmentsUseCase;
    private final GetStudentAssignmentsUseCase getStudentAssignmentsUseCase;
    private final StartAssignmentUseCase startAssignmentUseCase;
    private final CompleteAssignmentUseCase completeAssignmentUseCase;
    private final GetAssignmentSubmissionsUseCase getAssignmentSubmissionsUseCase;
    private final UpdateAssignmentUseCase updateAssignmentUseCase;
    private final DeleteAssignmentUseCase deleteAssignmentUseCase;

    public AssignmentController(
            CreateAssignmentUseCase createAssignmentUseCase,
            GetClassroomAssignmentsUseCase getClassroomAssignmentsUseCase,
            GetStudentAssignmentsUseCase getStudentAssignmentsUseCase,
            StartAssignmentUseCase startAssignmentUseCase,
            CompleteAssignmentUseCase completeAssignmentUseCase,
            GetAssignmentSubmissionsUseCase getAssignmentSubmissionsUseCase,
            UpdateAssignmentUseCase updateAssignmentUseCase,
            DeleteAssignmentUseCase deleteAssignmentUseCase
    ) {
        this.createAssignmentUseCase = createAssignmentUseCase;
        this.getClassroomAssignmentsUseCase = getClassroomAssignmentsUseCase;
        this.getStudentAssignmentsUseCase = getStudentAssignmentsUseCase;
        this.startAssignmentUseCase = startAssignmentUseCase;
        this.completeAssignmentUseCase = completeAssignmentUseCase;
        this.getAssignmentSubmissionsUseCase = getAssignmentSubmissionsUseCase;
        this.updateAssignmentUseCase = updateAssignmentUseCase;
        this.deleteAssignmentUseCase = deleteAssignmentUseCase;
    }

    @PostMapping("/api/v1/classrooms/{classroomId}/assignments")
    @Operation(summary = "Create assignment", description = "Creates a new assignment in a classroom. Only the teacher can create.")
    public ResponseEntity<AssignmentResponse> createAssignment(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable UUID classroomId,
            @RequestBody CreateAssignmentRequest request
    ) {
        var command = new CreateAssignmentUseCase.CreateAssignmentCommand(
                request.episodeId(),
                request.title(),
                request.instructions(),
                request.dueDate()
        );
        Assignment assignment = createAssignmentUseCase.execute(authUser.id(), classroomId, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(AssignmentResponse.fromDomain(assignment));
    }

    @GetMapping("/api/v1/classrooms/{classroomId}/assignments")
    @Operation(summary = "List classroom assignments", description = "Returns all assignments for a classroom.")
    public ResponseEntity<List<AssignmentWithStatsResponse>> getClassroomAssignments(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable UUID classroomId
    ) {
        List<GetClassroomAssignmentsUseCase.AssignmentWithStats> assignments =
                getClassroomAssignmentsUseCase.execute(classroomId);

        return ResponseEntity.ok(assignments.stream()
                .map(AssignmentWithStatsResponse::fromDomain)
                .toList());
    }

    @GetMapping("/api/v1/me/assignments")
    @Operation(summary = "My assignments", description = "Returns all assignments for the current student across all classrooms.")
    public ResponseEntity<List<StudentAssignmentResponse>> getMyAssignments(
            @CurrentUser AuthenticatedUser authUser
    ) {
        List<GetStudentAssignmentsUseCase.StudentAssignment> assignments =
                getStudentAssignmentsUseCase.execute(authUser.id());

        return ResponseEntity.ok(assignments.stream()
                .map(StudentAssignmentResponse::fromDomain)
                .toList());
    }

    @PutMapping("/api/v1/assignments/{assignmentId}")
    @Operation(summary = "Update assignment", description = "Updates an assignment. Only the teacher can update.")
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable UUID assignmentId,
            @RequestBody UpdateAssignmentRequest request
    ) {
        var command = new UpdateAssignmentUseCase.UpdateAssignmentCommand(
                request.title(),
                request.instructions(),
                request.dueDate()
        );
        Assignment assignment = updateAssignmentUseCase.execute(authUser.id(), assignmentId, command);
        return ResponseEntity.ok(AssignmentResponse.fromDomain(assignment));
    }

    @DeleteMapping("/api/v1/assignments/{assignmentId}")
    @Operation(summary = "Delete assignment", description = "Deletes an assignment. Only the teacher can delete.")
    public ResponseEntity<Void> deleteAssignment(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable UUID assignmentId
    ) {
        deleteAssignmentUseCase.execute(authUser.id(), assignmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/assignments/{assignmentId}/start")
    @Operation(summary = "Start assignment", description = "Marks an assignment as started by the student.")
    public ResponseEntity<SubmissionResponse> startAssignment(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable UUID assignmentId
    ) {
        AssignmentSubmission submission = startAssignmentUseCase.execute(authUser.id(), assignmentId);
        return ResponseEntity.ok(SubmissionResponse.fromDomain(submission));
    }

    @PostMapping("/api/v1/assignments/{assignmentId}/complete")
    @Operation(summary = "Complete assignment", description = "Marks an assignment as completed by the student.")
    public ResponseEntity<SubmissionResponse> completeAssignment(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable UUID assignmentId
    ) {
        AssignmentSubmission submission = completeAssignmentUseCase.execute(authUser.id(), assignmentId);
        return ResponseEntity.ok(SubmissionResponse.fromDomain(submission));
    }

    @GetMapping("/api/v1/assignments/{assignmentId}/submissions")
    @Operation(summary = "List submissions", description = "Returns all submissions for an assignment. Only the teacher can view.")
    public ResponseEntity<List<SubmissionWithStudentResponse>> getSubmissions(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable UUID assignmentId
    ) {
        List<GetAssignmentSubmissionsUseCase.SubmissionWithStudent> submissions =
                getAssignmentSubmissionsUseCase.execute(authUser.id(), assignmentId);

        return ResponseEntity.ok(submissions.stream()
                .map(SubmissionWithStudentResponse::fromDomain)
                .toList());
    }
}
