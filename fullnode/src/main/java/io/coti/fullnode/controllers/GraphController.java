package io.coti.fullnode.controllers;

import io.coti.basenode.services.liveview.data.GraphData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.coti.fullnode.services.NodeServiceManager.liveViewService;

@RequestMapping("/fullgraph")
@RestController
@Slf4j
public class GraphController {

    @GetMapping()
    public GraphData getFullGraph() {
        return liveViewService.getFullGraph();
    }
}
