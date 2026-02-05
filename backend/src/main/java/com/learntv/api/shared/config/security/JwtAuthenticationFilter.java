package com.learntv.api.shared.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final MockAuthProperties mockAuthProperties;
    private final SupabaseJwtProperties supabaseJwtProperties;
    private final SecretKey secretKey;

    public JwtAuthenticationFilter(
            MockAuthProperties mockAuthProperties,
            SupabaseJwtProperties supabaseJwtProperties
    ) {
        this.mockAuthProperties = mockAuthProperties;
        this.supabaseJwtProperties = supabaseJwtProperties;
        this.secretKey = supabaseJwtProperties.isConfigured()
                ? Keys.hmacShaKeyFor(supabaseJwtProperties.secret().getBytes(StandardCharsets.UTF_8))
                : null;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            // No token - check if mock auth should create default user
            if (mockAuthProperties.enabled()) {
                setMockAuthentication();
            }
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            AuthenticatedUser user = mockAuthProperties.enabled()
                    ? authenticateWithMock(token)
                    : authenticateWithSupabase(token);

            if (user != null) {
                setAuthentication(user);
            }
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private AuthenticatedUser authenticateWithMock(String token) {
        // In mock mode, we accept any token and use its value as user ID if it's a UUID
        // Otherwise, use the default mock user
        try {
            UUID userId = UUID.fromString(token);
            return new AuthenticatedUser(
                    userId,
                    "user-" + token.substring(0, 8) + "@learntv.local",
                    UserRole.valueOf(mockAuthProperties.defaultRole())
            );
        } catch (IllegalArgumentException e) {
            // Token is not a UUID, use default mock user
            return createDefaultMockUser();
        }
    }

    private AuthenticatedUser authenticateWithSupabase(String token) {
        if (secretKey == null) {
            log.error("Supabase JWT secret not configured");
            return null;
        }

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String userId = claims.getSubject();
        String email = claims.get("email", String.class);
        String roleStr = claims.get("role", String.class);

        UserRole role = UserRole.LEARNER;
        if (roleStr != null) {
            try {
                role = UserRole.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // Default to LEARNER if role is invalid
            }
        }

        return new AuthenticatedUser(UUID.fromString(userId), email, role);
    }

    private void setMockAuthentication() {
        setAuthentication(createDefaultMockUser());
    }

    private AuthenticatedUser createDefaultMockUser() {
        return new AuthenticatedUser(
                UUID.fromString(mockAuthProperties.defaultUserId()),
                mockAuthProperties.defaultEmail(),
                UserRole.valueOf(mockAuthProperties.defaultRole())
        );
    }

    private void setAuthentication(AuthenticatedUser user) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()));
        var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
