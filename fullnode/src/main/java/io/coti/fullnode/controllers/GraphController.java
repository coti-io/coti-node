package io.coti.fullnode.controllers;

import io.coti.basenode.data.GraphData;
import io.coti.basenode.services.LiveView.LiveViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RequestMapping("/fullgraph")
@RestController
@Slf4j
public class GraphController {

    @Autowired
    private LiveViewService liveViewService;

    @RequestMapping(method = GET)
    public GraphData getFullGraph() {
        log.info("graph : {}", liveViewService.getFullGraph());
        return liveViewService.getFullGraph();
    }
}
