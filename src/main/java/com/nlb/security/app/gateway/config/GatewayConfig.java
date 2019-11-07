package com.nlb.security.app.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
//public class GatewayConfig {
//    @Bean
//    public RouteLocator routeLocator(RouteLocatorBuilder routeLocatorBuilder){
//        return routeLocatorBuilder.routes()
//                .route(r ->r.path("/oauth/token")
//                        .uri("localhost:8888/oauth/token"))
//                .route(r ->r.path("/nlb")
//                        .uri("localhost:9999/nlb"))
//                .route(r ->r.path("/user")
//                        .uri("localhost:9999/user"))
//                .route(r ->r.path("/test")
//                        .uri("localhost:6666/test"))
//                .build();
//    }
//}
