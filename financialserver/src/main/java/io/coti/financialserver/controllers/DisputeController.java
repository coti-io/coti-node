package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static io.coti.financialserver.services.NodeServiceManager.disputeService;
import static io.coti.financialserver.services.NodeServiceManager.itemService;

@RestController
@RequestMapping("/dispute")
public class DisputeController {

    @PutMapping()
    public ResponseEntity<IResponse> createDispute(@Valid @RequestBody NewDisputeRequest newDisputeRequest) {
        return disputeService.createDispute(newDisputeRequest);
    }

    @PostMapping()
    public ResponseEntity<IResponse> getDisputes(@Valid @RequestBody GetDisputesRequest getDisputesRequest) {
        return disputeService.getDisputes(getDisputesRequest);
    }

    @PutMapping(path = "/item/update")
    public ResponseEntity<IResponse> updateItem(@Valid @RequestBody UpdateItemRequest request) {
        return itemService.updateItem(request);
    }

    @PutMapping(path = "/item/vote")
    public ResponseEntity<IResponse> itemVote(@Valid @RequestBody VoteRequest request) {
        return itemService.vote(request);
    }

    @PostMapping(path = "/history")
    public ResponseEntity<IResponse> getDisputeHistory(@Valid @RequestBody GetDisputeHistoryRequest request) {
        return disputeService.getDisputeHistory(request);
    }

}
