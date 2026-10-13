package com.rockhill.reception_hall.menu;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestControllerAdvice(assignableTypes = {
        MenuController.class, PackageController.class, AddOnController.class,
        DecorOptionController.class, PackagePricingTierController.class })
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CatalogExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handle(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", String.valueOf(ex.getReason())));
    }
}