package com.sportcenter.shift_manager.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class PingController {

    private static final Logger logger = LoggerFactory.getLogger(PingController.class);

    @Value("${ping.token}")
    private String validToken;

    @GetMapping("/api/ping")
    public String ping(@RequestHeader(value = "Authorization", required = true) String token) {
        logger.info("Token recibido: {}", token);
        logger.info("Token válido configurado: Bearer {}", validToken);
        if (token != null && token.equals("Bearer " + validToken)) {
            logger.info("Ping recibido exitosamente a las {}", new java.util.Date());
            return "pong";
        }
        logger.warn("Intento de ping con token inválido a las {}", new java.util.Date());
        return "Forbidden"; // Respuesta clara para 403
    }
}