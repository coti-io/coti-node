package io.coti.trustscore.controllers;

import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.trustscore.http.*;
import io.coti.trustscore.services.NetworkFeeService;
import io.coti.trustscore.services.RollingReserveService;
import io.coti.trustscore.services.TrustScoreService;
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
public class TrustScoreController {

    @Autowired
    private TrustScoreService trustScoreService;

    @RequestMapping(path = "/usertrustscore", method = RequestMethod.POST)
    public ResponseEntity<BaseResponse> getUserTrustScore(@Valid @RequestBody GetTrustScoreRequest request) {
        return trustScoreService.getUserTrustScore(request.userHash);
    }

    @RequestMapping(path = "/usertrustscore", method = RequestMethod.PUT)
    public ResponseEntity<BaseResponse> setKycTrustScore(@Valid @RequestBody SetKycTrustScoreRequest request) {
        return trustScoreService.setKycTrustScore(request);
    }

    @RequestMapping(path = "/transactiontrustscore", method = RequestMethod.POST)
    public ResponseEntity<IResponse> getTransactionTrustScore(@Valid @RequestBody GetTransactionTrustScoreRequest request) {
        return trustScoreService.getTransactionTrustScore(request.userHash, request.transactionHash, request.userSignature);
    }

    @RequestMapping(path = "/insertevent", method = RequestMethod.PUT)
    public ResponseEntity<BaseResponse> insertTrustScoreEvent(@Valid @RequestBody InsertEventRequest request) {
        return trustScoreService.addCentralServerEvent(request);
    }

    @RequestMapping(path = "/usertype", method = RequestMethod.PUT)
    public ResponseEntity<BaseResponse> setUserType(@Valid @RequestBody SetUserTypeRequest request) {
        return trustScoreService.setUserType(request);
    }
}