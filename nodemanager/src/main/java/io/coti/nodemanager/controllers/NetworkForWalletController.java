package io.coti.nodemanager.controllers;

import io.coti.nodemanager.data.NetworkDetailsForWallet;
import io.coti.nodemanager.services.interfaces.INodesManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NetworkForWalletController {

    @Autowired
    private INodesManagementService nodesService;

    @RequestMapping(path = "/wallet/network_details", method = RequestMethod.GET)
    public ResponseEntity<NetworkDetailsForWallet> getNetworkDetails() {
        return ResponseEntity.ok(nodesService.createNetworkDetailsForWallet());
    }

}
