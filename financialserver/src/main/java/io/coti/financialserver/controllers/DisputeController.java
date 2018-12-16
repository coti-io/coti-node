package io.coti.financialserver.controllers;

import io.coti.financialserver.http.NewDisputeRequest;
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

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<IResponse> createDispute(@Valid @RequestBody NewDisputeRequest newDisputeRequest) {

        return disputeService.createDispute(newDisputeRequest);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<IResponse> getDisputes(@Valid @RequestBody GetDisputesRequest getDisputesRequest) {

        return disputeService.getDisputes(getDisputesRequest);
    }
}
