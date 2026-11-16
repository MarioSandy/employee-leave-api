package com.hr.employee.services;

import com.hr.employee.dtos.request.LoginRequestDto;
import com.hr.employee.dtos.request.RegisterRequestDto;
import com.hr.employee.dtos.response.LoginResponseDto;
import com.hr.employee.entities.Employee;
import com.hr.employee.entities.User;
import com.hr.employee.entities.UserSession;
import com.hr.employee.enums.UserRoleEnum;
import com.hr.employee.repositories.EmployeeRepository;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserAgentAnalyzer userAgentAnalyzer;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public ResponseEntity<String> register(RegisterRequestDto registerRequestData) {
        Employee employee = employeeRepository.findById(registerRequestData.employeeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        String hashedPassword = passwordEncoder.encode(registerRequestData.password());

        User user = new User().builder()
            .name(registerRequestData.name())
            .username(registerRequestData.username())
            .password(hashedPassword)
            .employeeId(employee.getId())
            .role(UserRoleEnum.EMPLOYEE)
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
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException("User account not found."));

        String accessToken = jwtService.generateToken(
            userId, user.getEmployeeId(), sessionId, user.getRole(), "access", Duration.ofMinutes(15)
        );
        String refreshToken = jwtService.generateToken(
            userId, user.getEmployeeId(), sessionId, user.getRole(), "refresh", Duration.ofDays(30)
        );

        UserAgent userAgent = userAgentAnalyzer.parse(request.getHeader("User-Agent"));

        UserSession userSessions = new UserSession().builder()
            .user(user)
            .clientSessionId(sessionId)
            .clientName(userAgent.getValue(UserAgent.AGENT_NAME))
            .osName(userAgent.getValue(UserAgent.OPERATING_SYSTEM_NAME))
            .ipAddress(NetworkUtil.getIp(request))
            .token(refreshToken)
            .build();

        userSessionRepository.save(userSessions);

        return ResponseEntity.ok()
            .body(new LoginResponseDto(accessToken, refreshToken));
    }

    public ResponseEntity refresh(UUID userId, UUID sessionId, String presentedRefreshToken)
        throws BadRequestException {
        UserSession userSession = userSessionRepository.findByClientSessionIdAndDeletedAtIsNull(sessionId)
            .orElseThrow(() -> new BadRequestException("No session found"));

        if (!userId.equals(userSession.getUserId())
            || !presentedRefreshToken.equals(userSession.getToken())) {
            throw new BadCredentialsException("Invalid refresh token.");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException("User account not found."));

        String accessToken = jwtService.generateToken(
            userId, user.getEmployeeId(), sessionId, user.getRole(), "access", Duration.ofMinutes(15)
        );

        return ResponseEntity.ok()
            .body(new LoginResponseDto(accessToken, userSession.getToken()));
    }
}