package io.coti.financialserver.controllers;

import io.coti.basenode.http.GenerateTokenRequest;
import io.coti.basenode.http.GetTokenGenerationDataRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.services.CurrencyService;
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
@RequestMapping("/Tokens")
public class TokenGenerationController {

    @Autowired
    private CurrencyService currencyService;

    @RequestMapping(path = "/getUserTokenGenerations", method = RequestMethod.POST)
    public ResponseEntity<IResponse> getUserTokenGenerationData(@Valid @RequestBody GetTokenGenerationDataRequest getTokenGenerationDataRequest) {
        return currencyService.getUserTokenGenerationData(getTokenGenerationDataRequest);
    }

    @RequestMapping(path = "/generateToken", method = RequestMethod.POST)
    public ResponseEntity<IResponse> generateToken(GenerateTokenRequest generateTokenRequest) {
        return currencyService.generateToken(generateTokenRequest);
    }
}
