package io.coti.nodemanager.controllers;

import io.coti.basenode.data.FeeData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.http.SetNodeStakeAdminRequest;
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

    @PutMapping(path = "/stake")
    public ResponseEntity<IResponse> setNodeStake(@Valid @RequestBody SetNodeStakeAdminRequest request) {
        return stakingService.setNodeStake(request.getStakingNodeData());
    }

    @GetMapping(path = "/stake/list")
    public ResponseEntity<IResponse> getStakerList() {
        return stakingService.getStakerList();
    }
    @GetMapping(path = "/distributioncheck") // test distribution function
    public ResponseEntity<String> distributionCheck(@Valid @RequestBody FeeData request) {
        return stakingService.distributionCheck(request);
    }
}


