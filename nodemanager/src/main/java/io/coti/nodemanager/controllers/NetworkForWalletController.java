package io.coti.nodemanager.controllers;

import io.coti.nodemanager.data.SingleNodeDetailsForWallet;
import io.coti.nodemanager.services.interfaces.INodesManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class NetworkForWalletController {

    @Autowired
    private INodesManagementService nodesService;

    @RequestMapping(path = "/wallet/network_details", method = RequestMethod.GET)
    public Map<String, List<SingleNodeDetailsForWallet>> getNetworkDetails() {

        return nodesService.createNetworkDetailsForWallet();
    }

}
