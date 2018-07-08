package io.coti.fullnode.controllers;

import io.coti.common.data.GraphData;
import io.coti.fullnode.LiveView.LiveViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NodesController {
    @Autowired
    private LiveViewService liveViewService;

    @MessageMapping("/getfullgraph")
    @SendTo("/topic/fullGraph")
    public GraphData getGraph(String message) {
        GraphData data = liveViewService.getFullGraph();

        return data;
    }
}
