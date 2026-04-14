package com.ahatravel.customer.service;

import com.ahatravel.customer.dto.request.LoginRequest;
import com.ahatravel.customer.dto.request.RefreshTokenRequest;
import com.ahatravel.customer.dto.response.AuthResponse;
import com.ahatravel.customer.dto.response.UserResponse;
import com.ahatravel.customer.entity.User;
import com.ahatravel.customer.entity.UserSession;
import com.ahatravel.customer.exception.AuthenticationException;
import com.ahatravel.customer.repository.UserRepository;
import com.ahatravel.customer.repository.UserSessionRepository;
import com.ahatravel.customer.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    private static final String REFRESH_PREFIX = "refresh:token:";

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByEmailAndActiveTrue(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password");
        }

        String accessToken = tokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());

        String tokenId = UUID.randomUUID().toString();
        String refreshJwt = tokenProvider.generateRefreshTokenJwt(user.getId(), user.getEmail(), tokenId);

        // Store refresh token id in Redis
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + tokenId,
                user.getId().toString(),
                Duration.ofMillis(tokenProvider.getRefreshTokenExpiration()));

        // Persist session record
        UserSession session = UserSession.builder()
                .user(user)
                .refreshTokenId(tokenId)
                .ipAddress(getClientIp(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .expiresAt(LocalDateTime.now().plusSeconds(
                        tokenProvider.getRefreshTokenExpiration() / 1000))
                .build();
        sessionRepository.save(session);

        return buildAuthResponse(user, accessToken, refreshJwt);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        Claims claims;
        try {
            claims = tokenProvider.parseToken(request.getRefreshToken());
        } catch (JwtException ex) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        String tokenId = claims.getId();
        String storedUserId = redisTemplate.opsForValue().get(REFRESH_PREFIX + tokenId);
        if (storedUserId == null) {
            throw new AuthenticationException("Refresh token not found or already used");
        }

        Long userId = Long.parseLong(storedUserId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new AuthenticationException("Account is disabled");
        }

        // Rotate: revoke old refresh token
        redisTemplate.delete(REFRESH_PREFIX + tokenId);

        String newAccessToken = tokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String newTokenId = UUID.randomUUID().toString();
        String newRefreshJwt = tokenProvider.generateRefreshTokenJwt(
                user.getId(), user.getEmail(), newTokenId);

        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + newTokenId,
                user.getId().toString(),
                Duration.ofMillis(tokenProvider.getRefreshTokenExpiration()));

        return buildAuthResponse(user, newAccessToken, newRefreshJwt);
    }

    @Transactional
    public void logout(String accessToken, Long userId) {
        try {
            Claims claims = tokenProvider.parseToken(accessToken);
            String tokenId = claims.getId();
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + tokenId,
                        "1",
                        Duration.ofMillis(ttl));
            }
        } catch (JwtException ex) {
            log.debug("Token already invalid on logout: {}", ex.getMessage());
        }

        LocalDateTime now = LocalDateTime.now();
        sessionRepository.revokeAllByUserId(userId, now);
        // Clear all refresh tokens from Redis for this user
        // (sessions table handles lookup; Redis entries will expire naturally)
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiration() / 1000)
                .user(mapUserResponse(user))
                .build();
    }

    private UserResponse mapUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null && ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}
