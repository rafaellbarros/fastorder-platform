package br.com.rafaellbarros.fastorder.api.gateway.controller;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/fallback")
public class GatewayFallbackController {

    @GetMapping("/global")
    public Mono<ResponseEntity<Map<String, Object>>> globalFallback(ServerWebExchange exchange) {

        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

        String rawRouteId  = route != null ? route.getId() : "unknown-service";
        String serviceId = extractServiceId(rawRouteId);

        String originalPath = getOriginalPath(exchange);

        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", 503,
                "error", "SERVICE_UNAVAILABLE",
                "message", "Service temporarily unavailable. Please try again later.",
                "service", serviceId,
                "uriPath", originalPath
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body));
    }

    private static String getOriginalPath(ServerWebExchange exchange) {
        Set<URI> originalUris = exchange.getAttribute(
                ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);

        return originalUris != null && !originalUris.isEmpty()
                ? originalUris.iterator().next().getPath()
                : "unknown-path";
    }

    private String extractServiceId(String routeId) {

        if (routeId == null) {
            return "unknown-service";
        }

        int idx = routeId.lastIndexOf('_');

        if (idx == -1 || idx == routeId.length() - 1) {
            return routeId.toLowerCase();
        }

        return routeId.substring(idx + 1).toLowerCase();
    }

}



