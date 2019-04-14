package io.coti.fullnode.controllers;

import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.LiveView.data.GraphData;
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
        return liveViewService.getFullGraph();
    }
}
