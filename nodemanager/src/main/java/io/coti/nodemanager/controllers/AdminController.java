package io.coti.nodemanager.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.http.AddNodeBeginEventPairAdminRequest;
import io.coti.nodemanager.http.AddNodeEventAdminRequest;
import io.coti.nodemanager.http.SetNodeStakeAdminRequest;
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

    @PutMapping(path = "/addnodeevent")
    public ResponseEntity<IResponse> addNodeEventAdmin(@Valid @RequestBody AddNodeEventAdminRequest request) {
        return nodeManagementService.addNodeEventAdmin(request);
    }

    @PutMapping(path = "/addnodebegineventpair")
    public ResponseEntity<IResponse> addNodeBeginEventPairAdmin(@Valid @RequestBody AddNodeBeginEventPairAdminRequest request) {
        return nodeManagementService.addNodeBeginEventPairAdmin(request);
    }
}

