package io.coti.nodemanager.controllers;

import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.nodemanager.http.SetNodeStakeRequest;
import io.coti.nodemanager.services.StakingService;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@Slf4j
@RestController
@RequestMapping("/nodes")
public class NodeController {

    @Autowired
    private INodeManagementService nodeManagementService;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private StakingService stakingService;

    @PutMapping
    public ResponseEntity<String> addNode(@Valid @RequestBody NetworkNodeData networkNodeData) {

        log.info("New networkNodeData received: {} ", networkNodeData);
        return nodeManagementService.addNode(networkNodeData);
    }

    @GetMapping
    public ResponseEntity<NetworkData> getAllNodes() {
        return ResponseEntity.ok(networkService.getSignedNetworkData());
    }

    @PutMapping(path = "/stake")
    public ResponseEntity<IResponse> setNodeStake(@Valid @RequestBody SetNodeStakeRequest request) {
        return stakingService.setNodeStake(request);  // not checked
    }
}