package com.learntv.api.catalog.application.port;

import java.util.List;
import java.util.UUID;

public interface UserShowRepository {
    void addUserShow(UUID userId, UUID showId);
    List<UUID> findShowIdsByUserId(UUID userId);
}
