package io.coti.nodemanager.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.http.*;
import io.coti.nodemanager.services.NodeManagementService;
import io.coti.nodemanager.services.StakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private StakingService stakingService;
    @Autowired
    private NodeManagementService nodeManagementService;

    @PutMapping(path = "/stake")
    public ResponseEntity<IResponse> setNodeStake(@Valid @RequestBody SetNodeStakeAdminRequest request) {
        return stakingService.setNodeStake(request.getStakingNodeData());
    }

    @GetMapping(path = "/stake/list")
    public ResponseEntity<IResponse> getStakerList() {
        return stakingService.getStakerList();
    }

    @PutMapping(path = "/node/event")
    public ResponseEntity<IResponse> addSingleNodeEvent(@Valid @RequestBody AddNodeSingleEventRequest request) {
        return nodeManagementService.addSingleNodeEvent(request);
    }

    @PutMapping(path = "/node/event/pair")
    public ResponseEntity<IResponse> addPairNodeEvent(@Valid @RequestBody AddNodePairEventRequest request) {
        return nodeManagementService.addPairNodeEvent(request);
    }

    @DeleteMapping(path = "/node/blacklist")
    public ResponseEntity<IResponse> deleteBlacklistNode(@Valid @RequestBody DeleteBlacklistNodeRequest request) {
        return nodeManagementService.deleteBlacklistNode(request);
    }

    @PutMapping(path = "/node/update")
    public ResponseEntity<IResponse> updateNode(@Valid @RequestBody ReplaceNodeRequest request) {
        return nodeManagementService.updateNode(request);
    }
}
