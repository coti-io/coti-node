package io.coti.nodemanager.controllers;

import io.coti.nodemanager.http.GetNetworkDetailsResponse;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import io.coti.nodemanager.services.StakingService;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
public class NetworkForWalletController {

    @Autowired
    private INodeManagementService nodeManagementService;
    @Autowired
    private StakingService stakingService;

    @GetMapping(path = "/nodes")
    public ResponseEntity<GetNetworkDetailsResponse> getNetworkDetails() {
        GetNetworkDetailsResponse networkDetailsResponse = new GetNetworkDetailsResponse(nodeManagementService.getNetworkDetailsForWallet());
        return ResponseEntity.ok(networkDetailsResponse);
    }

    @GetMapping(path = "/onefullnode")
    public ResponseEntity<SingleNodeDetailsForWallet> getOneFullNode() {
        SingleNodeDetailsForWallet singleNodeDetailsForWallet = nodeManagementService.getOneNodeDetailsForWallet();
        return ResponseEntity.ok(singleNodeDetailsForWallet);
    }
}
