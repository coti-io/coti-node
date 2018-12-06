package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.crypto.GetDisputeCrypto;
import io.coti.financialserver.http.GetDisputeRequest;
import io.coti.financialserver.http.NewDisputeRequest;
import io.coti.financialserver.services.DisputeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    private static final String UNAUTHORIZED = "Unauthorized";
    @Autowired
    private DisputeService disputeService;

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<IResponse> createDispute(@Valid @RequestBody NewDisputeRequest newDisputeRequest) {

        return disputeService.createDispute(newDisputeRequest);
    }

    @RequestMapping(path = "/get", method = RequestMethod.POST)
    public ResponseEntity getDispute(@Valid @RequestBody GetDisputeRequest request) {

        GetDisputeCrypto disputeCrypto = new GetDisputeCrypto();
        disputeCrypto.signMessage(request);

        if (!disputeCrypto.verifySignature(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }

        return disputeService.getDispute(request.getUserHash(), request.getDisputeHash());
    }
}
