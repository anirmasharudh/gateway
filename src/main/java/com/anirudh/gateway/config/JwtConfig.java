package com.anirudh.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@Configuration
public class JwtConfig {

    // jwt.jwk-set-uri points at your identity provider's JWKS endpoint
    // (Keycloak/Cognito/Auth0/internal auth service — whichever issues the tokens).
    @Bean
    public ReactiveJwtDecoder jwtDecoder(@Value("${jwt.jwk-set-uri}") String jwkSetUri) {
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

}
