package com.vinay.AuthService.Config;

// Marks methods/classes that define Spring beans and app-level configuration.
import com.vinay.AuthService.Utils.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
// Declares this class as a source of bean definitions.
import org.springframework.context.annotation.Configuration;
// Main fluent API used to configure HTTP security rules.
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// Enables Spring Security's web security support and integration.
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// BCrypt implementation for hashing passwords securely.
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// Abstraction used by services to encode and verify passwords.
import org.springframework.security.crypto.password.PasswordEncoder;
// Represents the full configured web security filter chain.
import org.springframework.security.web.SecurityFilterChain;

// Registers this class as a configuration component in the Spring context.
@Configuration
// Turns on web security so Spring applies filter-based security to requests.
@EnableWebSecurity
// Enable method-level security annotations like @PreAuthorize
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // JWT filter bean injected from the context; it validates tokens per request.
    private final org.springframework.web.filter.OncePerRequestFilter jwtFilter;

    // Constructor injection ensures the filter is provided when this config is created.
    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        // Store the injected filter for later registration in the filter chain.
        this.jwtFilter = jwtFilter;
    }

    // Exposes PasswordEncoder as a bean so it can be autowired in services.
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt adds salt and adaptive cost, recommended for password storage.
        return new BCryptPasswordEncoder();
    }

    // Defines the HTTP security behavior for the entire application.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Start configuring HttpSecurity using the lambda-style DSL.
        http
            // Disable CSRF for stateless REST APIs (common when using JWT).
            .csrf(csrf -> csrf.disable())
            // Disable browser popup-style basic auth; JWT will handle auth instead.
            .httpBasic(basic -> basic.disable())
            // Disable default form login so Spring Security does not try to use HTML login flows.
            .formLogin(form -> form.disable())
            // Configure session management policies.
            .sessionManagement(session -> session.sessionCreationPolicy(
                // Tell Spring Security not to create HTTP sessions (stateless API).
                org.springframework.security.config.http.SessionCreationPolicy.STATELESS
            ))
            // Configure endpoint authorization rules.
            .authorizeHttpRequests(authz -> authz
                // Allow login and registration without authentication (public routes).
                // The login endpoint is on /api/users/login in this controller.
                .requestMatchers("/api/users/login", "/api/users/register", "/error").permitAll()
                // Require authentication for any other endpoint not matched above.
                .anyRequest().authenticated()
            )
            // Insert JWT filter before Spring's username/password auth filter.
            .addFilterBefore(
                // Custom filter that reads/validates bearer tokens from requests.
                jwtFilter,
                // Reference filter used as insertion point in the chain order.
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
            );

        // Build and return the immutable security filter chain.
        return http.build();
    }
}