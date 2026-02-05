package com.learntv.api.shared.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "supabase.jwt")
public record SupabaseJwtProperties(
        String secret,
        String issuer
) {
    public boolean isConfigured() {
        return secret != null && !secret.isBlank();
    }
}
