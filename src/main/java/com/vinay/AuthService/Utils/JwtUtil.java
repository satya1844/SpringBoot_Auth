package com.vinay.AuthService.Utils;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component

public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;


    //generate the token
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
    }

   //extract email from token
   public String extractEmail(String token) {
       return parseClaims(token).getSubject();
   }

   //extract expiration date from token
   public Date extractExpiration(String token) {
       return parseClaims(token).getExpiration();
   }

   private io.jsonwebtoken.Claims parseClaims(String token) {
       return Jwts.parserBuilder()
               .setSigningKey(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
               .build()
               .parseClaimsJws(token)
               .getBody();
   }

    //validate token
    public boolean validateToken(String token, String email) {
        String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }
    //is token expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String extractRole(String token) {

        return parseClaims(token).get("role", String.class);
    }
}





