package io.coti.trustscore.controllers;

import io.coti.basenode.http.GetTrustScoreRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.trustscore.http.GetTransactionTrustScoreRequest;
import io.coti.trustscore.http.SetKycTrustScoreRequest;
import io.coti.trustscore.http.SetUserTypeRequest;
import io.coti.trustscore.services.TrustScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
public class TrustScoreController {

    @Autowired
    private TrustScoreService trustScoreService;

    @PostMapping(path = "/usertrustscore")
    public ResponseEntity<IResponse> getUserTrustScore(@Valid @RequestBody GetTrustScoreRequest request) {
        return trustScoreService.getUserTrustScore(request.getUserHash());
    }

    @PutMapping(path = "/usertrustscore")
    public ResponseEntity<IResponse> setKycTrustScore(@Valid @RequestBody SetKycTrustScoreRequest request) {
        return trustScoreService.setKycTrustScore(request);
    }

    @PostMapping(path = "/transactiontrustscore")
    public ResponseEntity<IResponse> getTransactionTrustScore(@Valid @RequestBody GetTransactionTrustScoreRequest request) {
        return trustScoreService.getTransactionTrustScore(request);
    }

    @PutMapping(path = "/usertype")
    public ResponseEntity<IResponse> setUserType(@Valid @RequestBody SetUserTypeRequest request) {
        return trustScoreService.setUserType(request);
    }

    @PostMapping(path = "/usertscomponents")
    public ResponseEntity<IResponse> getUserTrustScoreComponents(@Valid @RequestBody GetTrustScoreRequest request) {
        return trustScoreService.getUserTrustScoreComponents(request.getUserHash());
    }
}