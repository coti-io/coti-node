package io.coti.nodemanager.controllers;

import io.coti.nodemanager.http.GetNetworkDetailsResponse;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static io.coti.nodemanager.services.NodeServiceManager.nodeManagementService;

@RestController
@RequestMapping("/wallet")
public class NetworkForWalletController {

    @GetMapping(path = "/nodes")
    public ResponseEntity<GetNetworkDetailsResponse> getNetworkDetails(@RequestParam(required = false) String healthState) {
        GetNetworkDetailsResponse networkDetailsResponse = new GetNetworkDetailsResponse(nodeManagementService.getNetworkDetailsForWallet(healthState));
        return ResponseEntity.ok(networkDetailsResponse);
    }

    @GetMapping(path = "/onefullnode")
    public ResponseEntity<SingleNodeDetailsForWallet> getOneFullNode() {
        SingleNodeDetailsForWallet singleNodeDetailsForWallet = nodeManagementService.getOneNodeDetailsForWallet();
        return ResponseEntity.ok(singleNodeDetailsForWallet);
    }

}
