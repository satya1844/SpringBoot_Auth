package com.vinay.AuthService.Utils;


import com.vinay.AuthService.Utils.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter
 * This filter intercepts every HTTP request and validates the JWT token
 * It runs ONCE per request (hence OncePerRequestFilter)
 * Extracted from Authorization header and validates it
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Inject JwtUtil bean to validate and parse JWT tokens
    private final JwtUtil jwtUtil;

    // Constructor with dependency injection
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip JWT processing for public auth endpoints so login/register can work without a token.
        String path = request.getServletPath();
        return "/api/users/login".equals(path)
                || "/api/users/register".equals(path)
                || "/error".equals(path);
    }

    /**
     * This method is called for every HTTP request
     * It processes the JWT token and sets authentication in the security context
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,           // The HTTP request object
            HttpServletResponse response,         // The HTTP response object
            FilterChain filterChain                // The filter chain to pass control to next filter
    ) throws ServletException, IOException {

        try {
            // STEP 1: Extract the JWT token from the Authorization header
            // The header format is: "Authorization: Bearer <jwt-token>"
            String authHeader = request.getHeader("Authorization");

            // STEP 2: Check if Authorization header exists and starts with "Bearer "
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // STEP 3: Extract the token by removing "Bearer " prefix
                // For example: "Bearer abc123xyz" → "abc123xyz"
                String token = authHeader.substring(7);  // 7 = length of "Bearer "

                // STEP 4: Extract the email from the JWT token
                // The email is stored as the "subject" claim in the JWT
                String email = jwtUtil.extractEmail(token);

                // STEP 5: Validate the token using JwtUtil
                // This checks if the email matches and token is not expired
                if (jwtUtil.validateToken(token, email)) {

                    // STEP 6: If token is valid, extract the role from the token claims
                    // We need to parse the token again to get the role claim
                    // (You'll need to add a method in JwtUtil for this)
                    String role = jwtUtil.extractRole(token);

                    // STEP 7: Create an authentication object
                    // This represents an authenticated user in Spring Security
                    // Parameters:
                    // - principal: the user identifier (email in this case)
                    // - credentials: null (not needed for stateless JWT auth)
                    // - authorities: the user's roles/permissions
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    email,                           // Principal (who is authenticated)
                                    null,                            // Credentials (not needed for JWT)
                                    Collections.singletonList(
                                            new SimpleGrantedAuthority("ROLE_" + role)  // User's role/authority
                                    )
                            );

                    // STEP 8: Set the authentication in the SecurityContext
                    // This tells Spring Security that the user is authenticated
                    // All subsequent requests in this thread will have access to this authentication
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                // If token validation fails, the request continues without authentication
                // The @authorizeHttpRequests in SecurityConfig will handle the authorization
            }
            // If no Authorization header or doesn't start with "Bearer ", continue without authentication

        } catch (Exception e) {
            // STEP 9: Catch any exceptions during token processing
            // This could be: invalid token, expired token, malformed token, etc.
            // We log it but don't stop the filter chain
            // Spring Security will handle the authorization check later
            System.err.println("Cannot set user authentication: " + e.getMessage());
        }

        // STEP 10: Pass the request to the next filter in the chain
        // This is essential - without this, the request will be stuck
        filterChain.doFilter(request, response);
    }
}