package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.*;
import io.coti.financialserver.services.DisputeService;
import io.coti.financialserver.services.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/dispute")
public class DisputeController {

    @Autowired
    private DisputeService disputeService;

    @Autowired
    private ItemService itemService;

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<IResponse> createDispute(@Valid @RequestBody NewDisputeRequest newDisputeRequest) {

        return disputeService.createDispute(newDisputeRequest);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<IResponse> getDisputes(@Valid @RequestBody GetDisputesRequest getDisputesRequest) {

        return disputeService.getDisputes(getDisputesRequest);
    }

    @RequestMapping(path = "/item/update", method = RequestMethod.PUT)
    public ResponseEntity<IResponse> updateItem(@Valid @RequestBody UpdateItemRequest request) {

        return itemService.updateItem(request);
    }

    @RequestMapping(path = "/item/vote", method = RequestMethod.PUT)
    public ResponseEntity<IResponse> itemVote(@Valid @RequestBody VoteRequest request) {

        return itemService.vote(request);
    }

    @RequestMapping(path = "/history", method = RequestMethod.POST)
    public ResponseEntity<IResponse> getDisputeHistory(@Valid @RequestBody GetDisputeHistoryRequest request) {

        return disputeService.getDisputeHistory(request);
    }

}
