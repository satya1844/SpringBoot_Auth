package com.vinay.AuthService.Service;

import com.vinay.AuthService.Entity.RefreshToken;
import com.vinay.AuthService.Repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    // expiry in minutes
    private final long refreshTokenDurationMinutes = 60 * 24 * 7; // 7 days

    public RefreshToken createRefreshToken(Long userId) {
        // delete previous tokens for user (optional)
        refreshTokenRepository.deleteByUserId(userId);

        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .userId(userId)
                .expiryDate(LocalDateTime.now().plusMinutes(refreshTokenDurationMinutes))
                .build();

        return refreshTokenRepository.save(token);
    }

    public boolean validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token).orElse(null);
    }
}

