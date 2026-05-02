package com.lumora.repository;
import com.lumora.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // ── Buscas simples ────────────────────────────────────────────────────────

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(User.Role role);
    Page<User> findByRoleOrderByFullNameAsc(User.Role role, Pageable pageable);
    List<User> findByEnabledTrue();
    List<User> findByAccountNonLockedFalse();
    Page<User> findByFullNameContainingIgnoreCase(String nome, Pageable pageable);

    // ── Controle de tentativas de login ───────────────────────────────────────

    @Modifying @Transactional
    @Query("UPDATE User u SET u.failedAttempts = u.failedAttempts + 1 WHERE u.email = :email")
    void incrementFailedAttempts(@Param("email") String email);

    @Modifying @Transactional
    @Query("""
        UPDATE User u
        SET u.failedAttempts = 0, u.lockTime = null, u.accountNonLocked = true
        WHERE u.email = :email
        """)
    void resetFailedAttempts(@Param("email") String email);

    @Modifying @Transactional
    @Query("""
        UPDATE User u
        SET u.accountNonLocked = false, u.lockTime = CURRENT_TIMESTAMP
        WHERE u.email = :email
        """)
    void lockAccount(@Param("email") String email);

    @Modifying @Transactional
    @Query("""
        UPDATE User u
        SET u.accountNonLocked = true, u.failedAttempts = 0, u.lockTime = null
        WHERE u.accountNonLocked = false AND u.lockTime < :antes
        """)
    int unlockExpiredAccounts(@Param("antes") LocalDateTime antes);

    // ── Gerenciamento administrativo ──────────────────────────────────────────

    @Modifying @Transactional
    @Query("UPDATE User u SET u.enabled = :status WHERE u.id = :id")
    void updateEnabledStatus(@Param("id") UUID id, @Param("status") boolean status);

    @Modifying @Transactional
    @Query("UPDATE User u SET u.role = :role WHERE u.id = :id")
    void updateRole(@Param("id") UUID id, @Param("role") User.Role role);

    @Modifying @Transactional
    @Query("UPDATE User u SET u.password = :hash WHERE u.id = :id")
    void updatePassword(@Param("id") UUID id, @Param("hash") String hash);

    // ── Relatórios ────────────────────────────────────────────────────────────

    @Query("SELECT u FROM User u WHERE u.createdAt >= :desde ORDER BY u.createdAt DESC")
    List<User> findNewUsersSince(@Param("desde") LocalDateTime desde);

    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countByRole();
}
