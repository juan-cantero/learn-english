package com.learntv.api.user.adapter.in.web;

import com.learntv.api.shared.config.security.AuthenticatedUser;
import com.learntv.api.shared.config.security.CurrentUser;
import com.learntv.api.user.application.usecase.*;
import com.learntv.api.user.domain.model.User;
import com.learntv.api.user.domain.model.UserRole;
import com.learntv.api.user.domain.model.UserStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "User", description = "Current user profile and settings")
public class UserController {

    private final GetOrCreateUserUseCase getOrCreateUserUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final UpgradeToTeacherUseCase upgradeToTeacherUseCase;
    private final GetUserStatsUseCase getUserStatsUseCase;

    public UserController(
            GetOrCreateUserUseCase getOrCreateUserUseCase,
            GetCurrentUserUseCase getCurrentUserUseCase,
            UpdateUserProfileUseCase updateUserProfileUseCase,
            UpgradeToTeacherUseCase upgradeToTeacherUseCase,
            GetUserStatsUseCase getUserStatsUseCase
    ) {
        this.getOrCreateUserUseCase = getOrCreateUserUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.upgradeToTeacherUseCase = upgradeToTeacherUseCase;
        this.getUserStatsUseCase = getUserStatsUseCase;
    }

    @GetMapping
    @Operation(summary = "Get current user profile", description = "Returns the authenticated user's profile. Creates user if first login.")
    public ResponseEntity<UserResponse> getCurrentUser(@CurrentUser AuthenticatedUser authUser) {
        // Get or create user on first access
        User user = getOrCreateUserUseCase.execute(
                authUser.id(),
                authUser.email(),
                mapRole(authUser.role())
        );
        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    @PutMapping
    @Operation(summary = "Update user profile", description = "Updates the authenticated user's profile information.")
    public ResponseEntity<UserResponse> updateProfile(
            @CurrentUser AuthenticatedUser authUser,
            @RequestBody UpdateProfileRequest request
    ) {
        var command = new UpdateUserProfileUseCase.UpdateProfileCommand(
                request.displayName(),
                request.avatarUrl(),
                request.preferredDifficulty()
        );
        User user = updateUserProfileUseCase.execute(authUser.id(), command);
        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    @PostMapping("/upgrade-to-teacher")
    @Operation(summary = "Upgrade to teacher", description = "Upgrades the current user from LEARNER to TEACHER role.")
    public ResponseEntity<UserResponse> upgradeToTeacher(@CurrentUser AuthenticatedUser authUser) {
        User user = upgradeToTeacherUseCase.execute(authUser.id());
        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get user statistics", description = "Returns the authenticated user's learning statistics.")
    public ResponseEntity<UserStatsResponse> getStats(@CurrentUser AuthenticatedUser authUser) {
        UserStats stats = getUserStatsUseCase.execute(authUser.id());
        return ResponseEntity.ok(UserStatsResponse.fromDomain(stats));
    }

    private UserRole mapRole(com.learntv.api.shared.config.security.UserRole securityRole) {
        return switch (securityRole) {
            case LEARNER -> UserRole.LEARNER;
            case TEACHER -> UserRole.TEACHER;
        };
    }
}
