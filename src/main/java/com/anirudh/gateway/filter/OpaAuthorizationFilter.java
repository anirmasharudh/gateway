package com.anirudh.gateway.filter;

import com.anirudh.gateway.dto.OpaDecision;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

// Runs after JwtAuthenticationFilter (which populates X-User-Id / X-User-Roles),
// before routing/CircuitBreaker filters. Denied requests never reach the breaker,
// so they're never counted against paymentServiceCB's failure rate.
@Component
public class OpaAuthorizationFilter implements GlobalFilter, Ordered {

    private static final int ORDER = -90;

    private final WebClient opaClient;

    public OpaAuthorizationFilter(WebClient.Builder builder, @Value("${opa.url}") String opaUrl) {
        this.opaClient = builder.baseUrl(opaUrl).build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        Map<String, Object> body = Map.of("input", Map.of(
                "method", request.getMethod().name(),
                "path", request.getPath().value(),
                "userId", String.valueOf(request.getHeaders().getFirst("X-User-Id")),
                "roles", String.valueOf(request.getHeaders().getFirst("X-User-Roles"))
        ));

        return opaClient.post()
                .uri("/v1/data/gateway/authz/allow")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(OpaDecision.class)
                .flatMap(decision -> {
                    if (Boolean.TRUE.equals(decision.result())) {
                        return chain.filter(exchange);
                    }
                    return forbidden(exchange);
                })
                .onErrorResume(e -> forbidden(exchange)); // fail closed if OPA is unreachable
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
