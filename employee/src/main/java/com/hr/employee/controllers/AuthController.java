package com.hr.employee.controllers;

import com.hr.employee.dtos.request.LoginRequestDto;
import com.hr.employee.dtos.request.RegisterRequestDto;
import com.hr.employee.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("register")
    public ResponseEntity<String> register(
        @Valid @RequestBody RegisterRequestDto registerData
    ) {
        return authService.register(registerData);
    }

    @PostMapping("login")
    public ResponseEntity login(
        HttpServletRequest request,
        @Valid @RequestBody LoginRequestDto loginData
    ) {
        return authService.login(request, loginData);
    }

    @PostMapping("refresh")
    public ResponseEntity refresh(
        @AuthenticationPrincipal Jwt jwt
    ) throws BadRequestException {
        return authService.refresh(
            UUID.fromString(jwt.getSubject()),
            UUID.fromString(jwt.getClaims().get("session_id").toString())
        );
    }
}
