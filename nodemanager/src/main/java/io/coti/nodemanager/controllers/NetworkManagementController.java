package io.coti.nodemanager.controllers;

import io.coti.basenode.data.NetworkDetails;
import io.coti.nodemanager.data.NodeHistoryData;
import io.coti.nodemanager.services.interfaces.INetworkHistoryService;
import io.coti.nodemanager.services.interfaces.INodesManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/management")

public class NetworkManagementController {

    @Autowired
    private INetworkHistoryService networkHistoryService;
    @Autowired
    private INodesManagementService managementService;


    @RequestMapping(path = "/network_history", method = RequestMethod.GET)
    public ResponseEntity<String> getNetworkHistory() {
        return new ResponseEntity<>(networkHistoryService.getNodesHistory().toString(),HttpStatus.OK);
    }

    @RequestMapping(path = "/full_network", method = RequestMethod.GET)
    public ResponseEntity<String> getFullNetworkSummary() {
        return ResponseEntity.ok(managementService.getAllNetworkData().getNetWorkSummaryString());
    }

    @RequestMapping(path = "/full_network/verbose", method = RequestMethod.GET)
    public ResponseEntity<NetworkDetails> getFullNetworkSummaryVerbose() {
        return ResponseEntity.ok(managementService.getAllNetworkData());
    }


}
