package com.foodrescue.service;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import com.foodrescue.dto.auth.AuthResponse;
import com.foodrescue.dto.auth.LoginRequest;
import com.foodrescue.dto.auth.RegisterRequest;
import com.foodrescue.entity.AppRole;
import com.foodrescue.entity.UserAccount;
import com.foodrescue.entity.UserStatus;
import com.foodrescue.exception.BadRequestException;
import com.foodrescue.exception.ConflictException;
import com.foodrescue.exception.ForbiddenException;
import com.foodrescue.exception.UnauthorizedException;
import com.foodrescue.repository.UserAccountRepository;
import com.foodrescue.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
        UserAccountRepository userAccountRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validatePasswordLength(request.password());
        if (request.role() == AppRole.ADMIN) {
            throw new ForbiddenException("Administrator registration is restricted");
        }
        if (userAccountRepository.existsByEmailIgnoreCase(request.email().trim())) {
            throw new ConflictException("An account with this email already exists");
        }

        UserAccount user = new UserAccount();
        user.setEmail(request.email().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(request.phone() == null || request.phone().isBlank() ? null : request.phone().trim());
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(new HashSet<>(Set.of(request.role())));

        UserAccount savedUser = userAccountRepository.save(user);
        return toAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        validatePasswordLength(request.password());
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(request.email().trim())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getStatus() != UserStatus.ACTIVE
            || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(UserAccount user) {
        return new AuthResponse(
            jwtTokenService.createToken(user),
            "Bearer",
            jwtTokenService.getExpirationSeconds(),
            user.getId(),
            user.getEmail(),
            Set.copyOf(user.getRoles())
        );
    }

    private void validatePasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadRequestException("Password must not exceed 72 UTF-8 bytes");
        }
    }
}
