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
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.AlgorithmParameters;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final MockAuthProperties mockAuthProperties;
    private final SupabaseJwtProperties supabaseJwtProperties;
    private final SecretKey hmacKey;
    private final ECPublicKey ecPublicKey;

    public JwtAuthenticationFilter(
            MockAuthProperties mockAuthProperties,
            SupabaseJwtProperties supabaseJwtProperties
    ) {
        this.mockAuthProperties = mockAuthProperties;
        this.supabaseJwtProperties = supabaseJwtProperties;
        this.hmacKey = supabaseJwtProperties.isConfigured()
                ? Keys.hmacShaKeyFor(supabaseJwtProperties.secret().getBytes(StandardCharsets.UTF_8))
                : null;
        this.ecPublicKey = buildEcPublicKey(supabaseJwtProperties);
    }

    private ECPublicKey buildEcPublicKey(SupabaseJwtProperties props) {
        // Local Supabase EC public key (P-256 curve)
        // These are the default local Supabase keys - x and y coordinates
        if (props.ecPublicKeyX() == null || props.ecPublicKeyY() == null) {
            return null;
        }
        try {
            byte[] xBytes = Base64.getUrlDecoder().decode(props.ecPublicKeyX());
            byte[] yBytes = Base64.getUrlDecoder().decode(props.ecPublicKeyY());

            BigInteger x = new BigInteger(1, xBytes);
            BigInteger y = new BigInteger(1, yBytes);

            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(new ECGenParameterSpec("secp256r1"));
            ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);

            ECPoint ecPoint = new ECPoint(x, y);
            ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, ecParameterSpec);

            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return (ECPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            log.warn("Failed to build EC public key: {}", e.getMessage());
            return null;
        }
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
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
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
        Claims claims = null;

        // Try ES256 first (local Supabase default)
        if (ecPublicKey != null) {
            try {
                claims = Jwts.parser()
                        .verifyWith(ecPublicKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                log.debug("JWT validated with ES256");
            } catch (JwtException e) {
                log.debug("ES256 validation failed, trying HS256: {}", e.getMessage());
            }
        }

        // Fallback to HS256
        if (claims == null && hmacKey != null) {
            try {
                claims = Jwts.parser()
                        .verifyWith(hmacKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                log.debug("JWT validated with HS256");
            } catch (JwtException e) {
                log.warn("HS256 validation failed: {}", e.getMessage());
            }
        }

        if (claims == null) {
            log.error("Could not validate JWT with any configured key");
            return null;
        }

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
