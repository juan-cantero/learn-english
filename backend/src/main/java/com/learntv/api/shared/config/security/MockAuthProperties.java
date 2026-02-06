package com.learntv.api.shared.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.mock")
public record MockAuthProperties(
        boolean enabled,
        String defaultUserId,
        String defaultEmail,
        String defaultRole
) {
    public MockAuthProperties {
        if (defaultUserId == null) {
            defaultUserId = "00000000-0000-0000-0000-000000000001";
        }
        if (defaultEmail == null) {
            defaultEmail = "dev@learntv.local";
        }
        if (defaultRole == null) {
            defaultRole = "LEARNER";
        }
    }
}
