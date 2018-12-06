package io.coti.financialserver.controllers;

import io.coti.financialserver.crypto.GetDisputeCrypto;
import io.coti.financialserver.crypto.NewDisputeCrypto;
import io.coti.financialserver.http.NewDisputeRequest;
import io.coti.financialserver.http.GetDisputeRequest;
import io.coti.financialserver.services.DisputeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/dispute")
public class DisputeController {

    private static final String UNAUTHORIZED = "Unauthorized";

    private DisputeService disputeService;

    public DisputeController() {
        disputeService = new DisputeService();
    }

    @RequestMapping(path = "/new", method = RequestMethod.POST)
    public ResponseEntity newDispute(@Valid @RequestBody NewDisputeRequest request) {

        NewDisputeCrypto disputeCrypto = new NewDisputeCrypto();
        disputeCrypto.signMessage(request);

        if ( !disputeCrypto.verifySignature(request) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }

        return disputeService.newDispute(request.getUserHash(), request.getTransactionHash(), request.getDisputeItems(), request.getAmount());
    }

    @RequestMapping(path = "/get", method = RequestMethod.POST)
    public ResponseEntity getDispute(@Valid @RequestBody GetDisputeRequest request) {

        GetDisputeCrypto disputeCrypto = new GetDisputeCrypto();
        disputeCrypto.signMessage(request);

        if ( !disputeCrypto.verifySignature(request) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }

        return disputeService.getDispute(request.getUserHash(), request.getDisputeHash());
    }
}
