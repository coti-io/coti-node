package io.coti.trustscore.controllers;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.http.GetTransactionTrustScoreRequest;
import io.coti.trustscore.http.GetTrustScoreRequest;
import io.coti.trustscore.http.InsertTrustScoreEventRequest;
import io.coti.trustscore.http.SetKycTrustScoreRequest;
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
    public ResponseEntity<BaseResponse> getTransactionTrustScore(@Valid @RequestBody GetTransactionTrustScoreRequest request) {
        return trustScoreService.getTransactionTrustScore(request.userHash, request.transactionHash,request.transactionTrustScoreSignature);
    }


    @RequestMapping(path = "/insertevent", method = RequestMethod.PUT)
    public ResponseEntity<BaseResponse> insertTrustScoreEvent(@Valid @RequestBody InsertTrustScoreEventRequest request) {
        return trustScoreService.addCentralServerEvent(request);
    }
}