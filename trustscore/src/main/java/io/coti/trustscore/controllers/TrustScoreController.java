package io.coti.trustscore.controllers;

import io.coti.basenode.http.GetTrustScoreRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.trustscore.http.*;
import io.coti.trustscore.services.TrustScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
public class TrustScoreController {

    @Autowired
    private TrustScoreService trustScoreService;

    @PostMapping(path = "/usertrustscore")
    public ResponseEntity<IResponse> getUserTrustScore(@Valid @RequestBody GetTrustScoreRequest request) {
        return trustScoreService.getUserTrustScore(request.userHash);
    }

    @PutMapping(path = "/usertrustscore")
    public ResponseEntity<IResponse> setKycTrustScore(@Valid @RequestBody SetKycTrustScoreRequest request) {
        return trustScoreService.setKycTrustScore(request);
    }

    @PostMapping(path = "/transactiontrustscore")
    public ResponseEntity<IResponse> getTransactionTrustScore(@Valid @RequestBody GetTransactionTrustScoreRequest request) {
        return trustScoreService.getTransactionTrustScore(request);
    }

    @PutMapping(path = "/insertdocument")
    public ResponseEntity<IResponse> insertDocumentScore(@Valid @RequestBody InsertDocumentScoreRequest request) {
        return trustScoreService.insertDocumentScore(request);
    }

    @PutMapping(path = "/insertevent")
    public ResponseEntity<IResponse> insertEventScore(@Valid @RequestBody InsertEventScoreRequest request) {
        return trustScoreService.insertEventScore(request);
    }

    @PutMapping(path = "/insertchargeback")
    public ResponseEntity<IResponse> insertChargeBackFrequencyBasedScore(@Valid @RequestBody InsertChargeBackFrequencyBasedScoreRequest request) {
        return trustScoreService.insertChargeBackFrequencyBasedScore(request);
    }

    @PutMapping(path = "/insertdebtscore")
    public ResponseEntity<IResponse> insertDebtBalanceBasedScore(@Valid @RequestBody InsertDebtBalanceBasedScoreRequest request) {
        return trustScoreService.insertDebtBalanceBasedScore(request);
    }

    @PutMapping(path = "/insertdepositscore")
    public ResponseEntity<IResponse> insertDepositBalanceBasedScore(@Valid @RequestBody InsertDepositBalanceBasedScoreRequest request) {
        return trustScoreService.insertDepositBalanceBasedScore(request);
    }

    @PutMapping(path = "/usertype")
    public ResponseEntity<IResponse> setUserType(@Valid @RequestBody SetUserTypeRequest request) {
        return trustScoreService.setUserType(request);
    }

    @PutMapping(path = "/userzerotrustflag")
    public ResponseEntity<IResponse> setUserZeroTrustFlag(@Valid @RequestBody SetUserZeroTrustFlagSignedRequest request) {
        return trustScoreService.setUserZeroTrustFlag(request);
    }

    @PostMapping(path = "/usertscomponents")
    public ResponseEntity<IResponse> getUserTrustScoreComponents(@Valid @RequestBody GetTrustScoreRequest request) {
        return trustScoreService.getUserTrustScoreComponents(request.userHash);
    }
}