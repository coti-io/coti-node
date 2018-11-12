package io.coti.nodemanager.controllers;

import io.coti.nodemanager.data.SingleNodeDetailsForWallet;
import io.coti.nodemanager.http.GetNetworkDetailsResponse;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wallet")
public class NetworkForWalletController {

    @Autowired
    private INodeManagementService nodesService;

    @RequestMapping(path = "/network_details", method = RequestMethod.GET)
    public GetNetworkDetailsResponse getNetworkDetails() {
        GetNetworkDetailsResponse networkDetailsResponse = new GetNetworkDetailsResponse(nodesService.createNetworkDetailsForWallet());
        return networkDetailsResponse;
    }

}
