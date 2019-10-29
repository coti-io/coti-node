package io.coti.financialserver.controllers;

import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.GenerateTokenFeeRequest;
import io.coti.financialserver.http.GenerateTokenRequest;
import io.coti.financialserver.http.GetCurrenciesRequest;
import io.coti.financialserver.http.GetUserTokensRequest;
import io.coti.financialserver.services.CurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/currencies")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    @PostMapping(path = "/token/user")
    public ResponseEntity<IResponse> getUserTokens(@Valid @RequestBody GetUserTokensRequest getUserTokensRequest) {
        return currencyService.getUserTokens(getUserTokensRequest);
    }

    @PutMapping(path = "/token/generate")
    public ResponseEntity<IResponse> generateToken(@Valid @RequestBody GenerateTokenRequest generateTokenRequest) {
        return currencyService.generateToken(generateTokenRequest);
    }

    @PostMapping(path = "/token/fee")
    public ResponseEntity<BaseResponse> getTokenFee(@Valid @RequestBody GenerateTokenFeeRequest generateTokenFeeRequest) {
        return currencyService.getTokenGenerationFee(generateTokenFeeRequest);
    }


    @PostMapping(path = "/wallet")
    public ResponseEntity<IResponse> getCurrenciesForWallet(@Valid @RequestBody GetCurrenciesRequest getCurrenciesRequest) {
        return currencyService.getCurrenciesForWallet(getCurrenciesRequest);
    }

}
