package io.coti.nodemanager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.NetworkDetails;
import io.coti.nodemanager.data.NodeHistoryData;
import io.coti.nodemanager.services.interfaces.INetworkHistoryService;
import io.coti.nodemanager.services.interfaces.INodesManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/management")
@Slf4j
public class NetworkManagementController {
    private int exp;

    private int exp2;

    @Autowired
    private INetworkHistoryService networkHistoryService;
    @Autowired
    private INodesManagementService managementService;

    @RequestMapping(path = "/network_history", method = RequestMethod.GET)
    public List<NodeHistoryData> getNetworkHistory() {
        return networkHistoryService.getNodesHistory();
    }

    @RequestMapping(path = "/full_network", method = RequestMethod.GET)
    public Map<String, List<String>> getFullNetworkSummary() {
        return managementService.getAllNetworkData().getNetWorkSummary();
    }

    @RequestMapping(path = "/full_network/verbose", method = RequestMethod.GET)
    public NetworkDetails getFullNetworkSummaryVerbose() {
        return managementService.getAllNetworkData();
    }


}
