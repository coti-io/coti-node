package io.coti.fullnode.controllers;

import io.coti.basenode.http.NodeInformationResponse;
import io.coti.basenode.services.interfaces.INodeInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nodeinformation")
public class NodeInformationController {
    @Autowired
    private INodeInformationService nodeInformationService;

    @GetMapping()
    public ResponseEntity<NodeInformationResponse> getNodeInfo() {
        return ResponseEntity.ok(nodeInformationService.getNodeInformation());
    }
}