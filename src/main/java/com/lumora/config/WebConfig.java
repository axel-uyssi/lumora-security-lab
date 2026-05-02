package com.lumora.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

// ─────────────────────────────────────────────────────────────────────────────
// CONFIG/WEBCONFIG.JAVA — Configurações web da aplicação
//
// CORS (Cross-Origin Resource Sharing):
//   O browser bloqueia requisições de origens diferentes por padrão.
//   Ex: frontend em localhost:3000 chamando API em localhost:8080 → bloqueado.
//   Aqui configuramos quais origens têm permissão.
//
// Recursos estáticos:
//   O frontend HTML (com.com.com.lumora.lumora.com.com.lumora.lumora-hotels.html) fica em src/main/resources/static
//   e é servido automaticamente pelo Spring Boot em /.
// ─────────────────────────────────────────────────────────────────────────────

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${com.com.com.lumora.lumora.com.com.lumora.lumora.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Origens permitidas (frontend)
                .allowedOriginPatterns(
                        "http://localhost:3000",    // React / Create React App
                        "http://localhost:5173",    // Vite / Vue / React + Vite
                        "http://localhost:4200",    // Angular
                        "https://*.com.com.com.lumora.lumora.com.com.lumora.lumora.com"      // produção
                )
                // Métodos HTTP permitidos
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                // Headers que o frontend pode enviar
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With")
                // Headers que o frontend pode LER nas respostas
                .exposedHeaders("X-Total-Count", "X-Page-Number")
                // Permite envio de cookies e credenciais
                .allowCredentials(true)
                // Tempo de cache das configurações CORS no browser (1 hora)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Arquivos estáticos em /static servidos em /
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}