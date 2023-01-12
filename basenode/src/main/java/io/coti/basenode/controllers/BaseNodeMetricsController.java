package io.coti.basenode.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static io.coti.basenode.services.BaseNodeServiceManager.scraperService;

@Slf4j
@RestController
@RequestMapping("/metrics")
public class BaseNodeMetricsController {

    @GetMapping()
    public String metrics(HttpServletRequest request) {
        return scraperService.getMetrics(request);
    }

}
