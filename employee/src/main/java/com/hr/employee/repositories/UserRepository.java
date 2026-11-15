package com.hr.employee.repositories;

import com.hr.employee.entities.User;
import com.hr.employee.enums.UserRoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    @Query(
        value =
            "SELECT EXISTS (" +
            "   SELECT 1 " +
            "   FROM User u " +
            "   WHERE u.id = :id " +
            "       AND u.role IN :roles" +
            ") "
    )
    boolean existsByUserIdAndRolesNative(
            @Param("id") UUID userId,
            @Param("role") Collection<UserRoleEnum> roles
    );
}
