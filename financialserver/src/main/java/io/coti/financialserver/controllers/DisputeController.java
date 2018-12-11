package io.coti.financialserver.controllers;

import lombok.extern.slf4j.Slf4j;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.DisputeRequest;
import io.coti.financialserver.services.DisputeService;

@Slf4j
@RestController
@RequestMapping("/dispute")
public class DisputeController {

    @Autowired
    private DisputeService disputeService;

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<IResponse> createDispute(@Valid @RequestBody DisputeRequest disputeRequest) {

        return disputeService.createDispute(disputeRequest);
    }

    @RequestMapping(path = "/getDisputeHashesOpenedByMe", method = RequestMethod.POST)
    public ResponseEntity<IResponse> getDisputeHashesOpenedByMe(@Valid @RequestBody DisputeRequest disputeRequest) {

        return disputeService.getDisputeHashesOpenedByMe(disputeRequest);
    }

    @RequestMapping(path = "/getDisputeHashesOpenedOnMe", method = RequestMethod.POST)
    public ResponseEntity<IResponse> getDisputeHashesOpenedOnMe(@Valid @RequestBody DisputeRequest disputeRequest) {

        return disputeService.getDisputeHashesOpenedOnMe(disputeRequest);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<IResponse> getDispute(@Valid @RequestBody DisputeRequest disputeRequest) {

        return disputeService.getDispute(disputeRequest);
    }
}
