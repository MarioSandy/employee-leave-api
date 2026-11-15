package com.hr.employee.aspects;

import com.hr.employee.annotations.RequireRole;
import com.hr.employee.enums.UserRoleEnum;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class UserRoleCheckAspect {

    @Before("@annotation(requireRoles)")
    public void checkMethodUserRoles(RequireRole requireRoles) {
        validateRole(requireRoles);
    }

    @Before("@within(requireRoles) && !@annotation(requireRoles)")
    public void checkClassUserRoles(RequireRole requireRoles) {
        validateRole(requireRoles);
    }

    private void validateRole(RequireRole requiredRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new AuthenticationCredentialsNotFoundException(
                "Unauthorized: No valid JWT authentication found."
            );
        }

        String roleClaim = jwtAuthentication.getToken().getClaimAsString("role");
        if (roleClaim == null || roleClaim.isBlank()) {
            throw new AccessDeniedException("Forbidden: JWT does not contain a role claim.");
        }

        final UserRoleEnum userRole;
        try {
            userRole = UserRoleEnum.valueOf(roleClaim);
        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException("Forbidden: JWT contains an invalid role claim.");
        }

        if (!Arrays.asList(requiredRole.value()).contains(userRole)) {
            throw new AccessDeniedException(
                "Forbidden: You do not have one of the required roles: "
                    + Arrays.toString(requiredRole.value())
            );
        }
    }
}
