package io.coti.nodemanager.controllers;

import io.coti.nodemanager.http.GetFullNetworkResponse;
import io.coti.nodemanager.http.GetFullNetworkSummaryVerboseResponse;
import io.coti.nodemanager.http.GetNetworkHistoryResponse;
import io.coti.nodemanager.services.interfaces.INetworkHistoryService;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/management")
@Slf4j
public class NetworkManagementController {

    @Autowired
    private INetworkHistoryService networkHistoryService;
    @Autowired
    private INodeManagementService managementService;

    @RequestMapping(path = "/network_history", method = RequestMethod.GET)
    public GetNetworkHistoryResponse getNetworkHistory() {
        GetNetworkHistoryResponse getNetworkHistoryResponse = new GetNetworkHistoryResponse(networkHistoryService.getNodesHistory());
        return getNetworkHistoryResponse;
    }

    @RequestMapping(path = "/full_network", method = RequestMethod.GET)
    public GetFullNetworkResponse getFullNetworkSummary() {
        GetFullNetworkResponse getFullNetworkResponse = new GetFullNetworkResponse(managementService.getAllNetworkData().getNetWorkSummary());
        return getFullNetworkResponse;
    }

    @RequestMapping(path = "/full_network/verbose", method = RequestMethod.GET)
    public GetFullNetworkSummaryVerboseResponse getFullNetworkSummaryVerbose() {
        GetFullNetworkSummaryVerboseResponse summaryVerboseResponse =
                new GetFullNetworkSummaryVerboseResponse(managementService.getAllNetworkData());
        return summaryVerboseResponse;
    }


}
