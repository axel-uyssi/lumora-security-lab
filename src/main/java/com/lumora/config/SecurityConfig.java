package com.lumora.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // ─────────────────────────────────────────────────────────────
    // 🔐 Password Encoder (RESOLVE SEU ERRO)
    // ─────────────────────────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─────────────────────────────────────────────────────────────
    // 🔐 Authentication Manager (necessário para login futuro)
    // ─────────────────────────────────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ─────────────────────────────────────────────────────────────
    // 🔐 Configuração de segurança HTTP
    // ─────────────────────────────────────────────────────────────
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // ❌ Desabilita CSRF (API REST)
                .csrf(csrf -> csrf.disable())

                // ❌ Sem sessão (JWT stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 🔓 Rotas públicas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",   // login / register
                                "/h2-console/**", // console H2
                                "/v3/api-docs/**",
                                "/swagger-ui/**"
                        ).permitAll()

                        // 🔒 resto precisa autenticação
                        .anyRequest().authenticated()
                )

                // ⚠️ necessário pro H2 funcionar
                .headers(headers ->
                        headers.frameOptions(frame -> frame.disable())
                );

        return http.build();
    }
}
