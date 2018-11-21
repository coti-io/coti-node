package io.coti.nodemanager.controllers;

import io.coti.nodemanager.http.GetNetworkDetailsResponse;
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
    private INodeManagementService nodesService;

    @GetMapping(path = "/network_details")
    public ResponseEntity<GetNetworkDetailsResponse> getNetworkDetails() {
        GetNetworkDetailsResponse networkDetailsResponse = new GetNetworkDetailsResponse(nodesService.createNetworkDetailsForWallet());
        return ResponseEntity.ok(networkDetailsResponse);
    }
}
