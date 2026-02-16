package com.learntv.api.shared.config;

import com.learntv.api.catalog.application.port.ShowRepository;
import com.learntv.api.catalog.application.port.UserShowRepository;
import com.learntv.api.catalog.application.usecase.BrowseCatalogUseCase;
import com.learntv.api.catalog.application.usecase.ViewShowDetailsUseCase;
import com.learntv.api.generation.application.port.out.ShowMetadataPort;
import com.learntv.api.generation.domain.service.EpisodeLessonGenerator;
import com.learntv.api.learning.application.port.EpisodeRepository;
import com.learntv.api.learning.application.port.LessonQueryPort;
import com.learntv.api.learning.application.usecase.CheckExerciseAnswerUseCase;
import com.learntv.api.learning.application.usecase.ViewEpisodeLessonUseCase;
import com.learntv.api.progress.application.port.UserProgressRepository;
import com.learntv.api.progress.application.usecase.GetUserProgressUseCase;
import com.learntv.api.progress.application.usecase.UpdateProgressUseCase;
import com.learntv.api.user.application.port.UserRepository;
import com.learntv.api.user.application.port.UserStatsRepository;
import com.learntv.api.user.application.usecase.*;
import com.learntv.api.classroom.application.port.AssignmentRepository;
import com.learntv.api.classroom.application.port.ClassroomRepository;
import com.learntv.api.classroom.application.port.ClassroomStudentRepository;
import com.learntv.api.classroom.application.port.SubmissionRepository;
import com.learntv.api.classroom.application.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    // ==================== Catalog Use Cases ====================

    @Bean
    public BrowseCatalogUseCase browseCatalogUseCase(ShowRepository showRepository,
                                                      UserShowRepository userShowRepository) {
        return new BrowseCatalogUseCase(showRepository, userShowRepository);
    }

    @Bean
    public ViewShowDetailsUseCase viewShowDetailsUseCase(ShowRepository showRepository,
                                                          EpisodeRepository episodeRepository,
                                                          ShowMetadataPort showMetadataPort) {
        return new ViewShowDetailsUseCase(showRepository, episodeRepository, showMetadataPort);
    }

    // ==================== Learning Use Cases ====================

    @Bean
    public ViewEpisodeLessonUseCase viewEpisodeLessonUseCase(LessonQueryPort lessonQueryPort,
                                                              UserProgressRepository progressRepository) {
        return new ViewEpisodeLessonUseCase(lessonQueryPort, progressRepository);
    }

    @Bean
    public CheckExerciseAnswerUseCase checkExerciseAnswerUseCase(LessonQueryPort lessonQueryPort,
                                                                   UserProgressRepository progressRepository) {
        return new CheckExerciseAnswerUseCase(lessonQueryPort, progressRepository);
    }

    // ==================== Progress Use Cases ====================

    @Bean
    public GetUserProgressUseCase getUserProgressUseCase(UserProgressRepository progressRepository) {
        return new GetUserProgressUseCase(progressRepository);
    }

    @Bean
    public UpdateProgressUseCase updateProgressUseCase(UserProgressRepository progressRepository) {
        return new UpdateProgressUseCase(progressRepository);
    }

    // ==================== Generation Domain Services ====================

    @Bean
    public EpisodeLessonGenerator episodeLessonGenerator() {
        return new EpisodeLessonGenerator();
    }

    // ==================== User Use Cases ====================

    @Bean
    public GetOrCreateUserUseCase getOrCreateUserUseCase(UserRepository userRepository,
                                                          UserStatsRepository userStatsRepository) {
        return new GetOrCreateUserUseCase(userRepository, userStatsRepository);
    }

    @Bean
    public GetCurrentUserUseCase getCurrentUserUseCase(UserRepository userRepository) {
        return new GetCurrentUserUseCase(userRepository);
    }

    @Bean
    public UpdateUserProfileUseCase updateUserProfileUseCase(UserRepository userRepository) {
        return new UpdateUserProfileUseCase(userRepository);
    }

    @Bean
    public UpgradeToTeacherUseCase upgradeToTeacherUseCase(UserRepository userRepository) {
        return new UpgradeToTeacherUseCase(userRepository);
    }

    @Bean
    public GetUserStatsUseCase getUserStatsUseCase(UserStatsRepository userStatsRepository) {
        return new GetUserStatsUseCase(userStatsRepository);
    }

    // ==================== Classroom Use Cases ====================

    @Bean
    public CreateClassroomUseCase createClassroomUseCase(ClassroomRepository classroomRepository,
                                                          UserRepository userRepository) {
        return new CreateClassroomUseCase(classroomRepository, userRepository);
    }

    @Bean
    public GetTeacherClassroomsUseCase getTeacherClassroomsUseCase(ClassroomRepository classroomRepository,
                                                                    ClassroomStudentRepository studentRepository) {
        return new GetTeacherClassroomsUseCase(classroomRepository, studentRepository);
    }

    @Bean
    public GetStudentClassroomsUseCase getStudentClassroomsUseCase(ClassroomRepository classroomRepository,
                                                                    ClassroomStudentRepository studentRepository) {
        return new GetStudentClassroomsUseCase(classroomRepository, studentRepository);
    }

    @Bean
    public JoinClassroomUseCase joinClassroomUseCase(ClassroomRepository classroomRepository,
                                                      ClassroomStudentRepository studentRepository) {
        return new JoinClassroomUseCase(classroomRepository, studentRepository);
    }

    @Bean
    public LeaveClassroomUseCase leaveClassroomUseCase(ClassroomRepository classroomRepository,
                                                        ClassroomStudentRepository studentRepository) {
        return new LeaveClassroomUseCase(classroomRepository, studentRepository);
    }

    @Bean
    public GetClassroomStudentsUseCase getClassroomStudentsUseCase(ClassroomRepository classroomRepository,
                                                                    ClassroomStudentRepository studentRepository,
                                                                    UserRepository userRepository) {
        return new GetClassroomStudentsUseCase(classroomRepository, studentRepository, userRepository);
    }

    @Bean
    public RemoveStudentUseCase removeStudentUseCase(ClassroomRepository classroomRepository,
                                                      ClassroomStudentRepository studentRepository) {
        return new RemoveStudentUseCase(classroomRepository, studentRepository);
    }

    @Bean
    public UpdateClassroomUseCase updateClassroomUseCase(ClassroomRepository classroomRepository) {
        return new UpdateClassroomUseCase(classroomRepository);
    }

    @Bean
    public RegenerateJoinCodeUseCase regenerateJoinCodeUseCase(ClassroomRepository classroomRepository) {
        return new RegenerateJoinCodeUseCase(classroomRepository);
    }

    @Bean
    public DeleteClassroomUseCase deleteClassroomUseCase(ClassroomRepository classroomRepository) {
        return new DeleteClassroomUseCase(classroomRepository);
    }

    // ==================== Assignment Use Cases ====================

    @Bean
    public CreateAssignmentUseCase createAssignmentUseCase(AssignmentRepository assignmentRepository,
                                                            ClassroomRepository classroomRepository) {
        return new CreateAssignmentUseCase(assignmentRepository, classroomRepository);
    }

    @Bean
    public GetClassroomAssignmentsUseCase getClassroomAssignmentsUseCase(AssignmentRepository assignmentRepository,
                                                                          ClassroomRepository classroomRepository,
                                                                          SubmissionRepository submissionRepository) {
        return new GetClassroomAssignmentsUseCase(assignmentRepository, classroomRepository, submissionRepository);
    }

    @Bean
    public GetStudentAssignmentsUseCase getStudentAssignmentsUseCase(AssignmentRepository assignmentRepository,
                                                                      ClassroomStudentRepository classroomStudentRepository,
                                                                      SubmissionRepository submissionRepository) {
        return new GetStudentAssignmentsUseCase(assignmentRepository, classroomStudentRepository, submissionRepository);
    }

    @Bean
    public StartAssignmentUseCase startAssignmentUseCase(AssignmentRepository assignmentRepository,
                                                          SubmissionRepository submissionRepository) {
        return new StartAssignmentUseCase(assignmentRepository, submissionRepository);
    }

    @Bean
    public CompleteAssignmentUseCase completeAssignmentUseCase(AssignmentRepository assignmentRepository,
                                                                SubmissionRepository submissionRepository,
                                                                UserProgressRepository progressRepository) {
        return new CompleteAssignmentUseCase(assignmentRepository, submissionRepository, progressRepository);
    }

    @Bean
    public GetAssignmentSubmissionsUseCase getAssignmentSubmissionsUseCase(AssignmentRepository assignmentRepository,
                                                                            ClassroomRepository classroomRepository,
                                                                            SubmissionRepository submissionRepository,
                                                                            UserRepository userRepository) {
        return new GetAssignmentSubmissionsUseCase(assignmentRepository, classroomRepository, submissionRepository, userRepository);
    }

    @Bean
    public UpdateAssignmentUseCase updateAssignmentUseCase(AssignmentRepository assignmentRepository,
                                                            ClassroomRepository classroomRepository) {
        return new UpdateAssignmentUseCase(assignmentRepository, classroomRepository);
    }

    @Bean
    public DeleteAssignmentUseCase deleteAssignmentUseCase(AssignmentRepository assignmentRepository,
                                                            ClassroomRepository classroomRepository) {
        return new DeleteAssignmentUseCase(assignmentRepository, classroomRepository);
    }
}
