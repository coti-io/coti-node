package io.coti.trustscore.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.trustscore.http.NetworkFeeRequest;
import io.coti.trustscore.http.NetworkFeeValidateRequest;
import io.coti.trustscore.services.NetworkFeeService;
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
public class FeeController {
    @Autowired
    NetworkFeeService feeService;

    @RequestMapping(path = "/networkFee", method = RequestMethod.PUT)
    public ResponseEntity<IResponse> createNetworkFeeConfirmation(@Valid @RequestBody NetworkFeeRequest request) {
        return feeService.createNetworkFee(request);
    }

    @RequestMapping(path = "/networkFee", method = RequestMethod.POST)
    public ResponseEntity<IResponse> validateNetworkFeeConfirmation(@Valid @RequestBody NetworkFeeValidateRequest request) {
        return feeService.validateNetworkFee(request);
    }
}
