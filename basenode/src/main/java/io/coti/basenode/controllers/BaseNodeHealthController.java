package io.coti.basenode.controllers;

import io.coti.basenode.services.BaseNodeMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.coti.basenode.services.BaseNodeServiceManager.monitorService;

@Slf4j
@RestController
@RequestMapping("/health")
public class BaseNodeHealthController {

    @GetMapping(path = "/total/state")
    public BaseNodeMonitorService.HealthState getNodeHash() {
        return monitorService.getLastTotalHealthState();
    }

}
