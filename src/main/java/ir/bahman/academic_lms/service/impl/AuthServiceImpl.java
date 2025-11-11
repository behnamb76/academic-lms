package ir.bahman.academic_lms.service.impl;

import ir.bahman.academic_lms.config.CustomUserDetailsService;
import ir.bahman.academic_lms.dto.LoginRequest;
import ir.bahman.academic_lms.exception.AccessDeniedException;
import ir.bahman.academic_lms.model.Account;
import ir.bahman.academic_lms.model.enums.AccountStatus;
import ir.bahman.academic_lms.repository.AccountRepository;
import ir.bahman.academic_lms.service.AuthService;
import ir.bahman.academic_lms.service.JwtService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AccountRepository accountRepository;

    public AuthServiceImpl(AuthenticationManager authManager,
                           JwtService jwtService,
                           CustomUserDetailsService userDetailsService, AccountRepository accountRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.accountRepository = accountRepository;
    }


    @Override
    public Map<String, String> login(LoginRequest dto) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        UserDetails user = userDetailsService.loadUserByUsername(dto.getUsername());
        Account account = accountRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Account not found!"));

        if (account.getStatus().equals(AccountStatus.ACTIVE)) {
            account.setAuthId(UUID.randomUUID());
            accountRepository.save(account);

            String accessToken = jwtService.generateAccessToken(account.getAuthId());
            String refreshToken = jwtService.generateRefreshToken(account.getAuthId());

            return Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
            );
        }
        throw new AccessDeniedException("Access denied. Your account is not active.");
    }

    @Override
    public Map<String, String> refresh(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken, "refresh")) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String authId = jwtService.extractAuthId(refreshToken);

        String newAccess = jwtService.generateAccessToken(UUID.fromString(authId));
        String newRefresh = jwtService.generateRefreshToken(UUID.fromString(authId));

        return Map.of(
                "accessToken", newAccess,
                "refreshToken", newRefresh
        );
    }

    @Override
    public Map<String, String> logout(String refreshToken) {
        if (refreshToken != null && jwtService.isTokenValid(refreshToken, "refresh")) {
            String authId = jwtService.extractAuthId(refreshToken);
            Account account = accountRepository.findByAuthId(UUID.fromString(authId))
                    .orElseThrow(() -> new EntityNotFoundException("Account not found"));
            return Map.of("message", "User " + account.getUsername() + " logged out successfully");
        }
        return Map.of("message", "Logged out");
    }
}
