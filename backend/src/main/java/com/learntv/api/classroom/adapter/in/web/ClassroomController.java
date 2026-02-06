package com.learntv.api.classroom.adapter.in.web;

import com.learntv.api.classroom.application.usecase.*;
import com.learntv.api.classroom.domain.model.Classroom;
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
@RequestMapping("/api/v1/classrooms")
@Tag(name = "Classrooms", description = "Classroom management operations")
public class ClassroomController {

    private final CreateClassroomUseCase createClassroomUseCase;
    private final GetTeacherClassroomsUseCase getTeacherClassroomsUseCase;
    private final GetStudentClassroomsUseCase getStudentClassroomsUseCase;
    private final JoinClassroomUseCase joinClassroomUseCase;
    private final LeaveClassroomUseCase leaveClassroomUseCase;
    private final GetClassroomStudentsUseCase getClassroomStudentsUseCase;
    private final RemoveStudentUseCase removeStudentUseCase;
    private final UpdateClassroomUseCase updateClassroomUseCase;
    private final RegenerateJoinCodeUseCase regenerateJoinCodeUseCase;
    private final DeleteClassroomUseCase deleteClassroomUseCase;

    public ClassroomController(
            CreateClassroomUseCase createClassroomUseCase,
            GetTeacherClassroomsUseCase getTeacherClassroomsUseCase,
            GetStudentClassroomsUseCase getStudentClassroomsUseCase,
            JoinClassroomUseCase joinClassroomUseCase,
            LeaveClassroomUseCase leaveClassroomUseCase,
            GetClassroomStudentsUseCase getClassroomStudentsUseCase,
            RemoveStudentUseCase removeStudentUseCase,
            UpdateClassroomUseCase updateClassroomUseCase,
            RegenerateJoinCodeUseCase regenerateJoinCodeUseCase,
            DeleteClassroomUseCase deleteClassroomUseCase
    ) {
        this.createClassroomUseCase = createClassroomUseCase;
        this.getTeacherClassroomsUseCase = getTeacherClassroomsUseCase;
        this.getStudentClassroomsUseCase = getStudentClassroomsUseCase;
        this.joinClassroomUseCase = joinClassroomUseCase;
        this.leaveClassroomUseCase = leaveClassroomUseCase;
        this.getClassroomStudentsUseCase = getClassroomStudentsUseCase;
        this.removeStudentUseCase = removeStudentUseCase;
        this.updateClassroomUseCase = updateClassroomUseCase;
        this.regenerateJoinCodeUseCase = regenerateJoinCodeUseCase;
        this.deleteClassroomUseCase = deleteClassroomUseCase;
    }

    @PostMapping
    @Operation(summary = "Create classroom", description = "Creates a new classroom. Only teachers can create classrooms.")
    public ResponseEntity<ClassroomResponse> createClassroom(
            @CurrentUser AuthenticatedUser authUser,
            @RequestBody CreateClassroomRequest request
    ) {
        var command = new CreateClassroomUseCase.CreateClassroomCommand(request.name(), request.description());
        Classroom classroom = createClassroomUseCase.execute(authUser.id(), command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ClassroomResponse.fromDomain(classroom));
    }

    @GetMapping
    @Operation(summary = "List my classrooms", description = "Returns classrooms owned by (teacher) or joined by (student) the current user.")
    public ResponseEntity<MyClassroomsResponse> getMyClassrooms(@CurrentUser AuthenticatedUser authUser) {
        List<GetTeacherClassroomsUseCase.ClassroomWithCount> teacherClassrooms =
                getTeacherClassroomsUseCase.execute(authUser.id());

        List<Classroom> studentClassrooms = getStudentClassroomsUseCase.execute(authUser.id());

        return ResponseEntity.ok(MyClassroomsResponse.from(teacherClassrooms, studentClassrooms));
    }

    @GetMapping("/{classroomId}")
    @Operation(summary = "Get classroom details", description = "Returns details of a specific classroom.")
    public ResponseEntity<ClassroomResponse> getClassroom(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable UUID classroomId
    ) {
        // For now, just return the classroom if user is owner
        // Could extend to allow members to see it too
        var classrooms = getTeacherClassroomsUseCase.execute(authUser.id());
        return classrooms.stream()
                .filter(c -> c.classroom().getId().value().equals(classroomId))
                .findFirst()
                .map(c -> ResponseEntity.ok(ClassroomResponse.fromDomainWithCount(c.classroom(), c.studentCount())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{classroomId}")
    @Operation(summary = "Update classroom", description = "Updates classroom name and description. Only the owner can update.")
    public ResponseEntity<ClassroomResponse> updateClassroom(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable UUID classroomId,
            @RequestBody UpdateClassroomRequest request
    ) {
        var command = new UpdateClassroomUseCase.UpdateClassroomCommand(request.name(), request.description());
        Classroom classroom = updateClassroomUseCase.execute(authUser.id(), classroomId, command);
        return ResponseEntity.ok(ClassroomResponse.fromDomain(classroom));
    }

    @DeleteMapping("/{classroomId}")
    @Operation(summary = "Delete classroom", description = "Deactivates a classroom. Only the owner can delete.")
    public ResponseEntity<Void> deleteClassroom(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable UUID classroomId
    ) {
        deleteClassroomUseCase.execute(authUser.id(), classroomId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{classroomId}/regenerate-code")
    @Operation(summary = "Regenerate join code", description = "Generates a new join code for the classroom.")
    public ResponseEntity<ClassroomResponse> regenerateJoinCode(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable UUID classroomId
    ) {
        Classroom classroom = regenerateJoinCodeUseCase.execute(authUser.id(), classroomId);
        return ResponseEntity.ok(ClassroomResponse.fromDomain(classroom));
    }

    @PostMapping("/join")
    @Operation(summary = "Join classroom", description = "Join a classroom using a join code.")
    public ResponseEntity<ClassroomResponse> joinClassroom(
            @CurrentUser AuthenticatedUser authUser,
            @RequestBody JoinClassroomRequest request
    ) {
        Classroom classroom = joinClassroomUseCase.execute(authUser.id(), request.joinCode());
        return ResponseEntity.ok(ClassroomResponse.fromDomain(classroom));
    }

    @DeleteMapping("/{classroomId}/leave")
    @Operation(summary = "Leave classroom", description = "Leave a classroom you've joined.")
    public ResponseEntity<Void> leaveClassroom(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable UUID classroomId
    ) {
        leaveClassroomUseCase.execute(authUser.id(), classroomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{classroomId}/students")
    @Operation(summary = "List students", description = "Returns all students in a classroom. Only the owner can view.")
    public ResponseEntity<List<StudentResponse>> getStudents(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable UUID classroomId
    ) {
        List<GetClassroomStudentsUseCase.StudentInfo> students =
                getClassroomStudentsUseCase.execute(authUser.id(), classroomId);

        return ResponseEntity.ok(students.stream()
                .map(StudentResponse::fromDomain)
                .toList());
    }

    @DeleteMapping("/{classroomId}/students/{studentId}")
    @Operation(summary = "Remove student", description = "Removes a student from the classroom. Only the owner can remove.")
    public ResponseEntity<Void> removeStudent(
            @CurrentUser AuthenticatedUser authUser,
            @PathVariable UUID classroomId,
            @PathVariable UUID studentId
    ) {
        removeStudentUseCase.execute(authUser.id(), classroomId, studentId);
        return ResponseEntity.noContent().build();
    }
}
