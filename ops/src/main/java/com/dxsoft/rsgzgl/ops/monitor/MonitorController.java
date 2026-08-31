package com.dxsoft.rsgzgl.ops.monitor;

import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private final MonitorService service;

    MonitorController(MonitorService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    MonitorOverview overview(@RequestParam(defaultValue = "120") int history) {
        return service.overview(history);
    }

    @PostMapping("/collect")
    MonitorSnapshotView collect() {
        return service.collectAndStore();
    }

    @GetMapping("/targets")
    List<MonitorTargetView> targets() {
        return service.listTargets();
    }

    @PostMapping("/targets")
    MonitorTargetView addTarget(@RequestBody MonitorTargetRequest request) {
        return service.addTarget(request);
    }

    @DeleteMapping("/targets/{id}")
    void deleteTarget(@PathVariable long id) {
        service.deleteTarget(id);
    }
}
