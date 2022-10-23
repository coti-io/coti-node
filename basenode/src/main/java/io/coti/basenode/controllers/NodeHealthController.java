package io.coti.basenode.controllers;

import io.coti.basenode.services.BaseNodeMonitorService;
import io.coti.basenode.services.interfaces.IMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/health")
public class NodeHealthController {

    @Autowired
    IMonitorService monitorService;

    @GetMapping(path = "/state")
    public BaseNodeMonitorService.HealthState getNodeHash() {
        return monitorService.getLastTotalHealthState();
    }

}
