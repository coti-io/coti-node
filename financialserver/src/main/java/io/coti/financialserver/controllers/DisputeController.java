package io.coti.financialserver.controllers;

import io.coti.financialserver.http.ItemRequest;
import io.coti.financialserver.http.NewDisputeRequest;
import io.coti.financialserver.http.VoteRequest;
import io.coti.financialserver.services.ItemService;
import lombok.extern.slf4j.Slf4j;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.GetDisputesRequest;
import io.coti.financialserver.services.DisputeService;

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

    @RequestMapping(path = "/item/new",method = RequestMethod.PUT)
    public ResponseEntity itemNew(@Valid @RequestBody ItemRequest request) {

        return itemService.itemNew(request);
    }

    @RequestMapping(path = "/item/update",method = RequestMethod.PUT)
    public ResponseEntity itemUpdate(@Valid @RequestBody ItemRequest request) {

        return itemService.itemUpdate(request);
    }

    @RequestMapping(path = "/item/vote", method = RequestMethod.PUT)
    public ResponseEntity itemVote(@Valid @RequestBody VoteRequest request) {

        return itemService.vote(request);
    }
}
