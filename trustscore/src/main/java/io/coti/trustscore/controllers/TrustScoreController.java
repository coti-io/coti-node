package io.coti.trustscore.controllers;

import io.coti.basenode.http.GetTrustScoreRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.trustscore.http.*;
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
    public ResponseEntity<IResponse> getUserTrustScore(@Valid @RequestBody GetTrustScoreRequest request) {
        return trustScoreService.getUserTrustScore(request.userHash);
    }

    @RequestMapping(path = "/usertrustscore", method = RequestMethod.PUT)
    public ResponseEntity<IResponse> setKycTrustScore(@Valid @RequestBody SetKycTrustScoreRequest request) {
        return trustScoreService.setKycTrustScore(request);
    }

    @RequestMapping(path = "/transactiontrustscore", method = RequestMethod.POST)
    public ResponseEntity<IResponse> getTransactionTrustScore(@Valid @RequestBody GetTransactionTrustScoreRequest request) {
        return trustScoreService.getTransactionTrustScore(request);
    }

    @RequestMapping(path = "/insertdocument", method = RequestMethod.PUT)
    public ResponseEntity<IResponse> insertDocumentScore(@Valid @RequestBody InsertDocumentScoreRequest request) {
        return trustScoreService.insertDocumentScore(request);
    }

    @RequestMapping(path = "/insertevent", method = RequestMethod.PUT)
    public ResponseEntity<IResponse> insertEventScore(@Valid @RequestBody InsertEventScoreRequest request) {
        return trustScoreService.insertEventScore(request);
    }

    @RequestMapping(path = "/insertchargeback", method = RequestMethod.PUT)
    public ResponseEntity<IResponse> insertChargeBackFrequencyBasedScore(@Valid @RequestBody InsertChargeBackFrequencyBasedScoreRequest request) {
        return trustScoreService.insertChargeBackFrequencyBasedScore(request);
    }

    @RequestMapping(path = "/insertdebtscore", method = RequestMethod.PUT)
    public ResponseEntity<IResponse> insertDebtBalanceBasedScore(@Valid @RequestBody InsertDebtBalanceBasedScoreRequest request) {
        return trustScoreService.insertDebtBalanceBasedScore(request);
    }

    @RequestMapping(path = "/insertdepositscore", method = RequestMethod.PUT)
    public ResponseEntity<IResponse> insertDepositBalanceBasedScore(@Valid @RequestBody InsertDepositBalanceBasedScoreRequest request) {
        return trustScoreService.insertDepositBalanceBasedScore(request);
    }

    @RequestMapping(path = "/usertype", method = RequestMethod.PUT)
    public ResponseEntity<IResponse> setUserType(@Valid @RequestBody SetUserTypeRequest request) {
        return trustScoreService.setUserType(request);
    }

    @RequestMapping(path = "/userzerotrustflag", method = RequestMethod.PUT)
    public ResponseEntity<IResponse> setUserZeroTrustFlag(@Valid @RequestBody SetUserZeroTrustFlagSignedRequest request) {
        return trustScoreService.setUserZeroTrustFlag(request);
    }

    @RequestMapping(path = "/usertscomponents", method = RequestMethod.POST)
    public ResponseEntity<IResponse> getUserTrustScoreComponents(@Valid @RequestBody GetTrustScoreRequest request) {
        return trustScoreService.getUserTrustScoreComponents(request.userHash);
    }
}