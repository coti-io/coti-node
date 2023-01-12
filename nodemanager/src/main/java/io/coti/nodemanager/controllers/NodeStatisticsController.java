package io.coti.nodemanager.controllers;


import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.http.GetNodeDetailsRequest;
import io.coti.nodemanager.http.GetNodeStatisticsRequest;
import io.coti.nodemanager.http.GetNodesActivityPercentageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.nodemanager.services.NodeServiceManager.networkHistoryService;

@RestController
@RequestMapping("/statistics")
@Slf4j
public class NodeStatisticsController {

    @PostMapping(path = "/events")
    public ResponseEntity<IResponse> getNodeEvents(@Valid @RequestBody GetNodeStatisticsRequest getNodeStatisticsRequest) {
        return networkHistoryService.getNodeEventsResponse(getNodeStatisticsRequest);
    }

    @PostMapping(path = "/days")
    public ResponseEntity<IResponse> getNodeDailyStats(@Valid @RequestBody GetNodeStatisticsRequest getNodeStatisticsRequest) {
        return networkHistoryService.getNodeDailyStats(getNodeStatisticsRequest);
    }

    @PostMapping(path = "/totals")
    public ResponseEntity<IResponse> getNodeStatsTotal(@Valid @RequestBody GetNodeStatisticsRequest getNodeStatisticsRequest) {
        return networkHistoryService.getNodeStatsTotal(getNodeStatisticsRequest);
    }

    @PostMapping(path = "/totalsByPercentage")
    public ResponseEntity<IResponse> getNodeActivityPercentage(@Valid @RequestBody GetNodeStatisticsRequest getNodeStatisticsRequest) {
        return networkHistoryService.getNodeActivityPercentage(getNodeStatisticsRequest);
    }

    @PostMapping(path = "/totalsByPercentageNodes")
    public ResponseEntity<IResponse> getNodeActivityPercentage(@Valid @RequestBody GetNodesActivityPercentageRequest getNodesActivityPercentageRequest) {
        return networkHistoryService.getNodesActivityPercentage(getNodesActivityPercentageRequest);
    }

    @PostMapping(path = "/totalsInSeconds")
    public ResponseEntity<IResponse> getNodeActivityInSeconds(@Valid @RequestBody GetNodeStatisticsRequest getNodeStatisticsRequest) {
        return networkHistoryService.getNodeActivityInSeconds(getNodeStatisticsRequest);
    }

    @PostMapping(path = "/totalsInSecondsByDay")
    public ResponseEntity<IResponse> getNodeActivityInSecondsByDay(@Valid @RequestBody GetNodeStatisticsRequest getNodeStatisticsRequest) {
        return networkHistoryService.getNodeActivityInSecondsByDay(getNodeStatisticsRequest);
    }

    @PostMapping(path = "/nodeActivationTime")
    public ResponseEntity<IResponse> getNodeActivationTime(@Valid @RequestBody GetNodeDetailsRequest getNodeDetailsRequest) {
        return networkHistoryService.getNodeActivationTime(getNodeDetailsRequest);
    }

    @PostMapping(path = "/nodeLastEvent")
    public ResponseEntity<IResponse> getNodeLastEvent(@Valid @RequestBody GetNodeDetailsRequest getNodeDetailsRequest) {
        return networkHistoryService.getNodeLastEvent(getNodeDetailsRequest);
    }

}
