package io.coti.nodemanager.controllers;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.http.BaseNodeHttpStringConstants;
import io.coti.basenode.services.interfaces.INetworkDetailsService;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@Slf4j
@RestController
@RequestMapping("/nodes")
public class NodeController {

    private final INodeManagementService nodesService;
    private final INetworkDetailsService networkDetailsService;

    @Autowired
    public NodeController(INodeManagementService nodesService, INetworkDetailsService networkDetailsService) {
        this.nodesService = nodesService;
        this.networkDetailsService = networkDetailsService;
    }

    @PutMapping
    public ResponseEntity<String> newNode(@Valid @RequestBody NetworkNodeData networkNodeData) {
        try {
            log.info("New networkNodeData received: {} ", networkNodeData);
            nodesService.newNode(networkNodeData);
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(BaseNodeHttpStringConstants.VALIDATION_EXCEPTION_MESSAGE);
        }
        return ResponseEntity.ok(BaseNodeHttpStringConstants.STATUS_SUCCESS);
    }

    @GetMapping
    public ResponseEntity<NetworkDetails> getAllNodes() {
        return ResponseEntity.ok(networkDetailsService.getNetworkDetails());
    }


}