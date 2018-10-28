package io.coti.nodemanager.controllers;

import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNode;
import io.coti.nodemanager.services.NodesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/nodes")
public class NodesController {

    @Autowired
    private NodesService nodesService;

    @RequestMapping(path = "/newNode", method = RequestMethod.POST)
    public ResponseEntity<NetworkData> newNode(@Valid @RequestBody NetworkNode networkNode) {
        nodesService.newNode(networkNode);
        return ResponseEntity.ok(nodesService.getAllNetworkData());
    }

    @RequestMapping(path = "/all", method = RequestMethod.GET)
    public NetworkData getAllNodes() {
        return nodesService.getAllNetworkData();
    }
}