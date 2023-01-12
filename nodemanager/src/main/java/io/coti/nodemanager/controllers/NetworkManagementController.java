package io.coti.nodemanager.controllers;

import io.coti.nodemanager.http.GetFullNetworkResponse;
import io.coti.nodemanager.http.GetFullNetworkSummaryVerboseResponse;
import io.coti.nodemanager.http.GetNetworkHistoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.coti.nodemanager.services.NodeServiceManager.networkHistoryService;
import static io.coti.nodemanager.services.NodeServiceManager.networkService;

@RestController
@RequestMapping("/management")
@Slf4j
public class NetworkManagementController {

    @GetMapping(path = "/network_history")
    public ResponseEntity<GetNetworkHistoryResponse> getNetworkHistory() {
        GetNetworkHistoryResponse getNetworkHistoryResponse = new GetNetworkHistoryResponse(networkHistoryService.getNodesHistory());
        return ResponseEntity.ok(getNetworkHistoryResponse);
    }

    @GetMapping(path = "/full_network")
    public ResponseEntity<GetFullNetworkResponse> getFullNetworkSummary() {
        GetFullNetworkResponse getFullNetworkResponse = new GetFullNetworkResponse(networkService.getNetworkSummary());
        return ResponseEntity.ok(getFullNetworkResponse);
    }

    @GetMapping(path = "/full_network/verbose")
    public ResponseEntity<GetFullNetworkSummaryVerboseResponse> getFullNetworkSummaryVerbose() {
        GetFullNetworkSummaryVerboseResponse summaryVerboseResponse =
                new GetFullNetworkSummaryVerboseResponse(networkService.getNetworkData());
        return ResponseEntity.ok(summaryVerboseResponse);
    }


}
