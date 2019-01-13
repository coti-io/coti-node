package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.GetDisputesRequest;
import io.coti.financialserver.services.DisputeService;
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
@RequestMapping("/event")
public class EventController {
    @Autowired
    DisputeService disputeService;

    @RequestMapping(path = "/unread", method = RequestMethod.POST)
    public ResponseEntity<IResponse> getUnreadEvents(@Valid @RequestBody GetDisputesRequest getDisputesRequest) {

        return disputeService.getDisputes(getDisputesRequest);
    }
}
