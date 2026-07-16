package org.xyjh.xyjhstartweb.duduplan.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dudu-plan")
public class DuduPlanHealthController {
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "serverTime", System.currentTimeMillis(), "protocolVersion", 2);
    }
}
