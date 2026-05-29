package com.vinay.AuthService.Repository;

import com.vinay.AuthService.Entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(Long userId);
}

//why do we need a new repo for refresh tokens
// We need a new repository for refresh tokens because they are a separate entity from users.
// The RefreshToken entity has its own fields (token, userId, expiryDate) and is stored in a different table in the database.
// By creating a separate repository, we can easily manage refresh tokens without affecting the user repository.
