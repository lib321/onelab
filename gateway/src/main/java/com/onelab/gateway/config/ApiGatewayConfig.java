package com.onelab.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/products/**")
                        .filters(f -> f
                                .prefixPath("/api")
                                .addResponseHeader("X-Powered-By", "Gateway Service")
                        )
                        .uri("http://localhost:8081")
                )
                .route(r -> r.path("/inventory/**")
                        .filters(f -> f
                                .prefixPath("/api")
                                .addResponseHeader("X-Powered-By", "Gateway Service")
                        )
                        .uri("http://localhost:8082")
                )
                .route(r -> r.path("/orders/**")
                        .filters(f -> f
                                .prefixPath("/api")
                                .addResponseHeader("X-Powered-By", "Gateway Service")
                        )
                        .uri("http://localhost:8083")
                )
                .route(r -> r.path("/**")
                        .filters(f -> f
                                .prefixPath("/auth")
                                .addResponseHeader("X-Powered-By", "Gateway Service")
                        )
                        .uri("http://localhost:8084")
                )
                .build();
    }
}
