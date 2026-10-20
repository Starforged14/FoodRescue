package com.foodrescue.security;

import java.time.Instant;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Base64;

import com.foodrescue.config.JwtProperties;
import com.foodrescue.entity.AppRole;
import com.foodrescue.entity.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import javax.crypto.SecretKey;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        if (properties.expiration().compareTo(Duration.ofSeconds(1)) < 0) {
            throw new IllegalStateException("security.jwt.expiration must be at least one second");
        }
        try {
            this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(properties.secret()));
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                "JWT_SECRET must be valid Base64 containing at least 32 bytes",
                exception
            );
        } catch (WeakKeyException exception) {
            throw new IllegalStateException(
                "JWT_SECRET must be valid Base64 containing at least 32 bytes",
                exception
            );
        }
    }

    public String createToken(UserAccount user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.expiration());

        return Jwts.builder()
            .subject(user.getId().toString())
            .claim("roles", user.getRoles().stream().map(Enum::name).toList())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .signWith(signingKey)
            .compact();
    }

    public Authentication authenticate(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        Object rolesClaim = claims.get("roles");
        if (!(rolesClaim instanceof List<?> roles)) {
            throw new JwtException("JWT roles claim is missing or invalid");
        }
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new JwtException("JWT subject is missing");
        }

        List<SimpleGrantedAuthority> authorities = roles.stream()
            .map(role -> {
                if (!(role instanceof String roleName)) {
                    throw new JwtException("JWT roles claim contains an invalid value");
                }
                AppRole roleValue = AppRole.valueOf(roleName);
                return new SimpleGrantedAuthority("ROLE_" + roleValue.name());
            })
            .toList();

        return new UsernamePasswordAuthenticationToken(
            subject,
            null,
            authorities
        );
    }

    public long getExpirationSeconds() {
        return properties.expiration().toSeconds();
    }
}
