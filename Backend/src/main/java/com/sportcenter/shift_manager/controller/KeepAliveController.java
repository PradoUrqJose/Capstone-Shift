package com.sportcenter.shift_manager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class KeepAliveController {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveController.class);

    @GetMapping("/api/keep-alive")
    public String keepAlive() {
        logger.info("Keep-alive solicitado a las {}", new java.util.Date());
        return "hola";
    }
}