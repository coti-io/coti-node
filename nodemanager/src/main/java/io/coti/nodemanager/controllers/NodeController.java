package io.coti.nodemanager.controllers;

import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.http.GetNetworkLastKnownNodesResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.http.SetNodeStakeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static io.coti.nodemanager.services.NodeServiceManager.*;

@Slf4j
@RestController
@RequestMapping("/nodes")
public class NodeController {

    @PutMapping
    public ResponseEntity<String> addNode(@Valid @RequestBody NetworkNodeData networkNodeData) {

        log.info("New networkNodeData received: {} ", networkNodeData);
        return nodeManagementService.addNode(networkNodeData);
    }

    @GetMapping("/last")
    public ResponseEntity<GetNetworkLastKnownNodesResponse> getLastKnownNodes() {
        return ResponseEntity.ok(networkService.getSignedNetworkLastKnownNodesResponse());
    }

    @GetMapping
    public ResponseEntity<NetworkData> getAllNodes() {
        return ResponseEntity.ok(networkService.getSignedNetworkData());
    }

    @GetMapping(path = "/blacklist")
    public ResponseEntity<IResponse> getBlacklistedNodes() {
        return nodeManagementService.getBlacklistedNodes();
    }

    @PutMapping(path = "/stake")
    public ResponseEntity<IResponse> setNodeStake(@Valid @RequestBody SetNodeStakeRequest request) {
        return stakingService.setNodeStake(request);  // not checked
    }
}
