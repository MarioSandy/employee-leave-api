package com.hr.employee.services;

import com.hr.employee.enums.UserRoleEnum;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.UUID;

@Service
@ConfigurationPropertiesScan
public class JwtService {
    private final JwtEncoder encoder;

    public JwtService(@Value("${spring.jwt.secret}") String secret) {
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    public String generateToken(
        UUID userId,
        UUID employeeId,
        UUID sessionId,
        UserRoleEnum role,
        String tokenType,
        TemporalAmount expiryTime
    ) {
        Instant now = Instant.now();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("splitBillApi")
            .issuedAt(now)
            .expiresAt(now.plus(expiryTime))
            .subject(userId.toString())
            .claim("employeeId", employeeId.toString())
            .claim("session_id", sessionId.toString())
            .claim("role", role.name())
            .claim("token_type", tokenType)
            .build();

        return this.encoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }
}
