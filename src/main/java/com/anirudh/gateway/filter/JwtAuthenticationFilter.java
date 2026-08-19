package com.anirudh.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// Runs first (very negative order) so unauthenticated traffic never reaches
// the OPA authorization check, the circuit breaker, or the load balancer.
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final int ORDER = -100;

    private final ReactiveJwtDecoder jwtDecoder;

    public JwtAuthenticationFilter(ReactiveJwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        return jwtDecoder.decode(token)
                .flatMap(jwt -> {
                    ServerWebExchange mutated = exchange.mutate()
                            .request(r -> r.headers(h -> {
                                h.set("X-User-Id", jwt.getSubject());
                                h.set("X-User-Roles", String.join(",", jwt.getClaimAsStringList("roles")));
                            }))
                            .build();
                    return chain.filter(mutated);
                })
                .onErrorResume(JwtException.class, e -> unauthorized(exchange));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
