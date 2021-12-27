package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.GetCurrenciesRequest;
import io.coti.financialserver.services.CurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/currencies")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    @PostMapping(path = "/wallet")
    public ResponseEntity<IResponse> getCurrenciesForWallet(@Valid @RequestBody GetCurrenciesRequest getCurrenciesRequest) {
        return currencyService.getCurrenciesForWallet(getCurrenciesRequest);
    }

}
