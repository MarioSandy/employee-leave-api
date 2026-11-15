package com.hr.employee.services;

import com.hr.employee.dtos.request.LoginRequestDto;
import com.hr.employee.dtos.request.RegisterRequestDto;
import com.hr.employee.dtos.response.LoginResponseDto;
import com.hr.employee.entities.User;
import com.hr.employee.entities.UserSession;
import com.hr.employee.enums.UserRoleEnum;
import com.hr.employee.repositories.UserRepository;
import com.hr.employee.repositories.UserSessionRepository;
import com.hr.employee.utils.NetworkUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserAgentAnalyzer userAgentAnalyzer;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<String> register(RegisterRequestDto registerRequestData) {
        // Hash password using Argon2
        String hashedPassword = passwordEncoder.encode(registerRequestData.password());

        User user = new User().builder()
            .name(registerRequestData.name())
            .username(registerRequestData.username())
            .password(hashedPassword)
            .build();

        userRepository.save(user);
        return ResponseEntity.ok("Register success");
    }

    public ResponseEntity login(HttpServletRequest request, LoginRequestDto loginRequestData) {
        Authentication authentication =
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequestData.username(),
                    loginRequestData.password()
                )
            );

        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.fromString(authentication.getName());

        String accessToken = jwtService.generateToken(userId, sessionId, Duration.ofMinutes(15));
        String refreshToken = jwtService.generateToken(userId, sessionId, Duration.ofDays(30));

        UserAgent userAgent = userAgentAnalyzer.parse(request.getHeader("User-Agent"));
        User user = userRepository.getReferenceById(userId);

        UserSession userSessions = new UserSession().builder()
            .user(user)
            .clientSessionId(sessionId)
            .clientName(userAgent.getValue(UserAgent.AGENT_NAME))
            .osName(userAgent.getValue(UserAgent.OPERATING_SYSTEM_NAME))
            .ipAddress(NetworkUtil.getIp(request))
            .token(refreshToken).build();

        userSessionRepository.save(userSessions);

        return ResponseEntity.ok()
            .body(
                new LoginResponseDto(
                    accessToken,
                    refreshToken
                )
            );
    }

    public ResponseEntity refresh(UUID userId, UUID sessionId) throws BadRequestException {
        UserSession userSession = userSessionRepository.findByClientSessionIdAndDeletedAtIsNull(sessionId)
                .orElseThrow(() -> new BadRequestException("No session found"));

        String accessToken = jwtService.generateToken(userId, sessionId, Duration.ofMinutes(15));

        return ResponseEntity.ok()
            .body(
                new LoginResponseDto(
                    accessToken,
                    userSession.getToken()
                )
            );
    }

    public boolean roleCheck(UUID userId, UserRoleEnum[] requiredUserRoles) {
        return userRepository.existsByUserIdAndRolesNative(userId, List.of(requiredUserRoles));
    }
}
