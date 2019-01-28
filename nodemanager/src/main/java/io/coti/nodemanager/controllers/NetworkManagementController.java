package io.coti.nodemanager.controllers;

import io.coti.basenode.services.interfaces.INetworkDetailsService;
import io.coti.nodemanager.http.GetFullNetworkResponse;
import io.coti.nodemanager.http.GetFullNetworkSummaryVerboseResponse;
import io.coti.nodemanager.http.GetNetworkHistoryResponse;
import io.coti.nodemanager.services.interfaces.INetworkHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/management")
@Slf4j
public class NetworkManagementController {

    @Autowired
    private INetworkHistoryService networkHistoryService;

    @Autowired
    private INetworkDetailsService networkDetailsService;

    @GetMapping(path = "/network_history")
    public ResponseEntity<GetNetworkHistoryResponse> getNetworkHistory() {
        GetNetworkHistoryResponse getNetworkHistoryResponse = new GetNetworkHistoryResponse(networkHistoryService.getNodesHistory());
        return ResponseEntity.ok(getNetworkHistoryResponse);
    }

    @GetMapping(path = "/full_network")
    public ResponseEntity<GetFullNetworkResponse> getFullNetworkSummary() {
        GetFullNetworkResponse getFullNetworkResponse = new GetFullNetworkResponse(networkDetailsService.getNetworkSummary(networkDetailsService.getNetworkData()));
        return ResponseEntity.ok(getFullNetworkResponse);
    }

    @GetMapping(path = "/full_network/verbose")
    public ResponseEntity<GetFullNetworkSummaryVerboseResponse> getFullNetworkSummaryVerbose() {
        GetFullNetworkSummaryVerboseResponse summaryVerboseResponse =
                new GetFullNetworkSummaryVerboseResponse(networkDetailsService.getNetworkData());
        return ResponseEntity.ok(summaryVerboseResponse);
    }


}
