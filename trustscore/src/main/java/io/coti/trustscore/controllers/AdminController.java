package io.coti.trustscore.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.trustscore.http.PurgeUserRequest;
import io.coti.trustscore.http.SetUserZeroTrustFlagRequest;
import io.coti.trustscore.services.TrustScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private TrustScoreService trustScoreService;

    @PutMapping(path = "/userzerotrustflag")
    public ResponseEntity<IResponse> setUserZeroTrustFlag(@Valid @RequestBody SetUserZeroTrustFlagRequest request) {
        return trustScoreService.setUserZeroTrustFlag(request);
    }

    @PutMapping(path = "/purgeuser")
    public ResponseEntity<IResponse> purgeUser(@Valid @RequestBody PurgeUserRequest request) {
        return trustScoreService.purgeUser(request);

    }
}

