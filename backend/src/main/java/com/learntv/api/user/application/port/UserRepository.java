package com.learntv.api.user.application.port;

import com.learntv.api.user.domain.model.User;
import com.learntv.api.user.domain.model.UserId;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findById(UserId id);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    User save(User user);

    boolean existsById(UserId id);
}
