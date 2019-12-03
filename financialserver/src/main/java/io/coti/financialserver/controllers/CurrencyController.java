package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.*;
import io.coti.financialserver.services.CurrencyService;
import io.coti.financialserver.services.MintingService;
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
    @Autowired
    private MintingService mintingService;

    @PostMapping(path = "/token/user")
    public ResponseEntity<IResponse> getUserTokens(@Valid @RequestBody GetUserTokensRequest getUserTokensRequest) {
        return currencyService.getUserTokens(getUserTokensRequest);
    }

    @PutMapping(path = "/token/generate")
    public ResponseEntity<IResponse> generateToken(@Valid @RequestBody GenerateTokenRequest generateTokenRequest) {
        return currencyService.generateToken(generateTokenRequest);
    }

    @PostMapping(path = "/token/generate/fee")
    public ResponseEntity<IResponse> getTokenGenerationFee(@Valid @RequestBody GenerateTokenFeeRequest generateTokenFeeRequest) {
        return currencyService.getTokenGenerationFee(generateTokenFeeRequest);
    }

    @PostMapping(path = "/token/mint/quote")
    public ResponseEntity<IResponse> getTokenMintingFeeQuote(@Valid @RequestBody GetTokenMintingFeeQuoteRequest getTokenMintingFeeQuoteRequest) {
        return mintingService.getTokenMintingFeeQuote(getTokenMintingFeeQuoteRequest);
    }

    @PostMapping(path = "/token/mint/fee")
    public ResponseEntity<IResponse> getTokenMintingFee(@Valid @RequestBody TokenMintingFeeRequest tokenMintingFeeRequest) {
        return mintingService.getTokenMintingFee(tokenMintingFeeRequest);
    }

    @GetMapping(path = "/token/mint/history")
    public ResponseEntity<IResponse> getTokenMintingHistory(@Valid @RequestBody GetMintingHistoryRequest getMintingHistoryRequest) {
        return mintingService.getTokenMintingHistory(getMintingHistoryRequest);
    }

    @PostMapping(path = "/wallet")
    public ResponseEntity<IResponse> getCurrenciesForWallet(@Valid @RequestBody GetCurrenciesRequest getCurrenciesRequest) {
        return currencyService.getCurrenciesForWallet(getCurrenciesRequest);
    }

}
