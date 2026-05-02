package com.lumora.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────────────────────
// MODEL/USER.JAVA — Entidade que representa o usuário do sistema
//
// Implementa UserDetails → integração com Spring Security.
// O Spring usa getUsername() e getPassword() internamente para autenticar.
//
// @Entity   → JPA mapeia esta classe para a tabela "users" no banco
// @Table    → define nome da tabela e índices
// @Builder  → permite: User.builder().email("x").build()
// ─────────────────────────────────────────────────────────────────────────────

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email", unique = true)
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    // UUID como PK — mais seguro que Long sequencial
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Email
    @NotBlank
    @Column(unique = true, nullable = false, length = 254)
    private String email;

    // Sempre armazenado como hash BCrypt — nunca texto puro
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.GUEST;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @Builder.Default
    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Builder.Default
    @Column(name = "failed_attempts")
    private int failedAttempts = 0;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── UserDetails (Spring Security) ────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() { return email; }

    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    // ── Role do usuário ───────────────────────────────────────────────────────
    public enum Role {
        GUEST,    // Hóspede — pode reservar e avaliar
        CURATOR,  // Curador — pode gerenciar hotéis
        ADMIN     // Administrador — acesso total
    }
}