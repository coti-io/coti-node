package io.coti.trustscore.controllers;

import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.GetTransactionTrustScoreRequest;
import io.coti.basenode.http.GetTrustScoreRequest;
import io.coti.basenode.http.SetKycTrustScoreRequest;
import io.coti.trustscore.services.TrustScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

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
        return trustScoreService.getTransactionTrustScore(request.userHash, request.transactionHash);
    }

    @InitBinder
    public void activateDirectFieldAccess(WebDataBinder dataBinder) {
        dataBinder.initDirectFieldAccess();
    }
}