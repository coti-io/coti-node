package io.coti.trustscore.controllers;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.http.*;
import io.coti.trustscore.services.FeesService;
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
public class FeesController {
    @Autowired
    FeesService feesService;




    @RequestMapping(path = "/rollingReservesFee", method = RequestMethod.PUT)
    public ResponseEntity<BaseResponse> createRollingReserveFee(@Valid @RequestBody RollingReserveRequest request) {
        return feesService.createRollingReserveFee(request);
    }

    @RequestMapping(path = "/networkFee", method = RequestMethod.PUT)
    public ResponseEntity<BaseResponse> createNetworkFeeConfirmation(@Valid @RequestBody NetworkFeeRequest request) {
        return feesService.createNetworkFee(request);
    }


    @RequestMapping(path = "/rollingReservesFee", method = RequestMethod.POST)
    public ResponseEntity<BaseResponse> validateRollingReserveFee(@Valid @RequestBody RollingReserveValidateRequest request) throws ClassNotFoundException {
        return feesService.validateRollingReserve(request);
    }

    @RequestMapping(path = "/networkFee", method = RequestMethod.POST)
    public ResponseEntity<BaseResponse> validateNetworkFeeConfirmation(@Valid @RequestBody NetworkFeeValidateRequest request) throws ClassNotFoundException {
        return feesService.validateNetworkFee(request);
    }
}
