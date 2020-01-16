package io.coti.nodemanager.controllers;


import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.http.GetNodeDailyStatisticsResponse;
import io.coti.nodemanager.http.GetNodeEventStatisticsResponse;
import io.coti.nodemanager.http.GetNodeStatisticsRequest;
import io.coti.nodemanager.http.GetNodeStatisticsResponse;
import io.coti.nodemanager.services.interfaces.INetworkHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/statistics")
@Slf4j
public class NodeStatisticsController {

    @Autowired
    private INetworkHistoryService networkHistoryService;

    @PostMapping(path = "/events")
    public ResponseEntity<GetNodeEventStatisticsResponse> getNodeEvents(@Valid @RequestBody GetNodeStatisticsRequest getNodeStatisticsRequest) {
        GetNodeEventStatisticsResponse getNodeEventStatisticsResponse = new GetNodeEventStatisticsResponse(networkHistoryService.getNodeEvents(getNodeStatisticsRequest));
        return ResponseEntity.ok(getNodeEventStatisticsResponse);
    }

    @PostMapping(path = "/days")
    public ResponseEntity<GetNodeDailyStatisticsResponse> getNodeDailyStats(@Valid @RequestBody GetNodeStatisticsRequest getNodeStatisticsRequest) {
        GetNodeDailyStatisticsResponse getNodeDailyStatisticsResponse = new GetNodeDailyStatisticsResponse(networkHistoryService.getNodeDailyStats(getNodeStatisticsRequest));
        return ResponseEntity.ok(getNodeDailyStatisticsResponse);
    }

    @PostMapping(path = "/totals")
    public ResponseEntity<GetNodeStatisticsResponse> getNodeStatsTotal(@Valid @RequestBody GetNodeStatisticsRequest getNodeStatisticsRequest) {
        GetNodeStatisticsResponse getNodeStatisticsResponse = new GetNodeStatisticsResponse(networkHistoryService.getNodeStatsTotal(getNodeStatisticsRequest));
        return ResponseEntity.ok(getNodeStatisticsResponse);
    }

    @PostMapping(path = "/totalsByPercentage")
    public ResponseEntity<IResponse> getNodeActivityPercentage(@Valid @RequestBody GetNodeStatisticsRequest getNodeStatisticsRequest) {
        return networkHistoryService.getNodeActivityPercentage(getNodeStatisticsRequest);
    }

    @PostMapping(path = "/totalsInSeconds")
    public ResponseEntity<IResponse> getNodeActivityInSeconds(@Valid @RequestBody GetNodeStatisticsRequest getNodeStatisticsRequest) {
        return networkHistoryService.getNodeActivityInSeconds(getNodeStatisticsRequest);
    }

    @PostMapping(path = "/totalsInSecondsByDay")
    public ResponseEntity<IResponse> getNodeActivityInSecondsByDay(@Valid @RequestBody GetNodeStatisticsRequest getNodeStatisticsRequest) {
        return networkHistoryService.getNodeActivityInSecondsByDay(getNodeStatisticsRequest);
    }
}
