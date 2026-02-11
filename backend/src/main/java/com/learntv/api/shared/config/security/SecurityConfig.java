package com.learntv.api.shared.config.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({MockAuthProperties.class, SupabaseJwtProperties.class})
public class SecurityConfig implements WebMvcConfigurer {

    private final MockAuthProperties mockAuthProperties;
    private final SupabaseJwtProperties supabaseJwtProperties;

    public SecurityConfig(
            MockAuthProperties mockAuthProperties,
            SupabaseJwtProperties supabaseJwtProperties
    ) {
        this.mockAuthProperties = mockAuthProperties;
        this.supabaseJwtProperties = supabaseJwtProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless API
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless session (JWT-based)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/**", "/health", "/h2-console/**").permitAll()

                        // Public API endpoints (read-only catalog browsing)
                        .requestMatchers(HttpMethod.GET, "/api/v1/shows/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/audio/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tts/**").permitAll()

                        // Generation endpoints (for development, may restrict later)
                        .requestMatchers("/api/v1/generation/**").permitAll()

                        // All other API endpoints require authentication
                        .requestMatchers("/api/**").authenticated()

                        // Default: permit
                        .anyRequest().permitAll()
                )

                // Add JWT filter before username/password authentication
                .addFilterBefore(
                        jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class
                )

                // Allow H2 console frames (dev only)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(mockAuthProperties, supabaseJwtProperties);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }
}
