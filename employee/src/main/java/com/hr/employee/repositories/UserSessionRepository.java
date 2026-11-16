package com.hr.employee.repositories;

import com.hr.employee.entities.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByClientSessionIdAndDeletedAtIsNull(UUID clientSessionId);

    @Transactional
    @Modifying
    @Query(value =
        "UPDATE UserSession us " +
        "   SET deletedAt = :now " +
        "WHERE userId = :id " +
        "   AND deletedAt IS NULL "
    )
    int logoutAll(
        @Param("id") UUID userId,
        @Param("now") Instant now
    );
}
