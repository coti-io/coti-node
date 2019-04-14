package io.coti.basenode.controllers;

import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.LiveView.data.GraphData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class LiveViewController {
    @Autowired
    private LiveViewService liveViewService;

    @MessageMapping("/getfullgraph")
    @SendTo("/topic/fullGraph")
    public GraphData getGraph(String message) {
        GraphData data = liveViewService.getFullGraph();

        return data;
    }
}
