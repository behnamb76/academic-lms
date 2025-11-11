package ir.bahman.academic_lms.service;

import ir.bahman.academic_lms.dto.LoginRequest;

import java.util.Map;

public interface AuthService {
    Map<String, String> login(LoginRequest dto);
    Map<String, String> refresh(String refreshToken);
    Map<String, String> logout(String refreshToken);
}
