package io.coti.basenode.controllers;

import io.coti.basenode.services.liveview.data.GraphData;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import static io.coti.basenode.services.BaseNodeServiceManager.liveViewService;

@Controller
public class LiveViewController {

    @MessageMapping("/getfullgraph")
    @SendTo("/topic/fullGraph")
    public GraphData getGraph(String message) {
        return liveViewService.getFullGraph();
    }
}
