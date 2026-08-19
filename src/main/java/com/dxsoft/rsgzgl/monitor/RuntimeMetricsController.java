package com.dxsoft.rsgzgl.monitor;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
class RuntimeMetricsController {

    private final RuntimeMetricsService service;

    RuntimeMetricsController(RuntimeMetricsService service) {
        this.service = service;
    }

    @GetMapping("/internal/runtime")
    RuntimeMetrics runtime(HttpServletRequest request) {
        if (!LoopbackAccess.allowed(request)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return service.collect();
    }
}
