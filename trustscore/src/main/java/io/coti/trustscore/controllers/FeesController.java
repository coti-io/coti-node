package io.coti.trustscore.controllers;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.http.GetFeesBaseTransactionRequest;
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

    @RequestMapping(path = "/networkFeesBaseTransaction", method = RequestMethod.POST)
    public ResponseEntity<BaseResponse> getNetworkFeeBaseTransaction(@Valid @RequestBody GetFeesBaseTransactionRequest request) {
        return feesService.getFeesBaseTransactions(request.getFullNodeFeeBaseTransactionData());
    }

    @RequestMapping(path = "/rollingReservesFeeBaseTransaction", method = RequestMethod.POST)
    public ResponseEntity<BaseResponse> getRollingReserveBaseTransaction(@Valid @RequestBody GetFeesBaseTransactionRequest request) {
        return feesService.getFeesBaseTransactions(request.getFullNodeFeeBaseTransactionData());
    }

    @RequestMapping(path = "/rollingReservesFeeConfirmation", method = RequestMethod.POST)
    public ResponseEntity<BaseResponse> getRollingReserveFeeConfirmation(@Valid @RequestBody GetFeesBaseTransactionRequest request) {
        return  null;
    }

    @RequestMapping(path = "/networkFeeConfirmation", method = RequestMethod.POST)
    public ResponseEntity<BaseResponse> getNetworkFeeConfirmation(@Valid @RequestBody GetFeesBaseTransactionRequest request) {
        return  null;
    }
}
