package io.coti.trustscore.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.trustscore.http.NetworkFeeRequest;
import io.coti.trustscore.http.NetworkFeeValidateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.trustscore.services.NodeServiceManager.feeService;

@RestController
public class FeeController {

    @PutMapping(path = "/networkFee")
    public ResponseEntity<IResponse> createNetworkFeeConfirmation(@Valid @RequestBody NetworkFeeRequest request) {
        return feeService.createNetworkFee(request);
    }

    @PostMapping(path = "/networkFee")
    public ResponseEntity<IResponse> validateNetworkFeeConfirmation(@Valid @RequestBody NetworkFeeValidateRequest request) {
        return feeService.validateNetworkFee(request);
    }
}
