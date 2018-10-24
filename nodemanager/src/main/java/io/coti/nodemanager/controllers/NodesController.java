package io.coti.nodemanager.controllers;

import io.coti.basenode.data.Network;
import io.coti.basenode.data.Node;
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
    public ResponseEntity<Network> newNode(@Valid @RequestBody Node node) {
        nodesService.newNode(node);
        return ResponseEntity.ok(nodesService.getAllNodes());
    }

    @RequestMapping(path = "/all", method = RequestMethod.GET)
    public Network getAllNodes() {
        return nodesService.getAllNodes();
    }
}