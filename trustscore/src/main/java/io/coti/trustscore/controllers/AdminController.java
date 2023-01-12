package io.coti.trustscore.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.trustscore.http.SetUserZeroTrustFlagRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.trustscore.services.NodeServiceManager.trustScoreService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @PutMapping(path = "/userzerotrustflag")
    public ResponseEntity<IResponse> setUserZeroTrustFlag(@Valid @RequestBody SetUserZeroTrustFlagRequest request) {
        return trustScoreService.setUserZeroTrustFlag(request);
    }
}

