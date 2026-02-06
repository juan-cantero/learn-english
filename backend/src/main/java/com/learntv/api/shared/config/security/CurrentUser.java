package com.learntv.api.shared.config.security;

import java.lang.annotation.*;

/**
 * Annotation to inject the current authenticated user into controller methods.
 *
 * Usage:
 * <pre>
 * {@code
 * @GetMapping("/me")
 * public UserResponse getCurrentUser(@CurrentUser AuthenticatedUser user) {
 *     return userService.getUser(user.id());
 * }
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
