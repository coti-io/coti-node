package io.coti.fullnode.controllers;

import io.coti.basenode.http.NodeInformationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.coti.fullnode.services.NodeServiceManager.nodeInformationService;

@RestController
@RequestMapping("/nodeinformation")
public class NodeInformationController {

    @GetMapping()
    public ResponseEntity<NodeInformationResponse> getNodeInfo() {
        return ResponseEntity.ok(nodeInformationService.getNodeInformation());
    }
}