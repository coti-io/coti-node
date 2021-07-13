package io.coti.basenode.controllers;

import io.coti.basenode.services.interfaces.IMetricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
public class BaseNodeMetricsController {

    @Autowired
    IMetricsService metricsService;

    @GetMapping(path = "/metrics")
    public String metrics(HttpServletRequest request) {
        return metricsService.getMetrics(request);
    }

}
