package io.coti.financialserver.controllers;

import io.coti.basenode.http.GetCurrenciesRequest;
import io.coti.basenode.http.interfaces.IResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.financialserver.services.NodeServiceManager.currencyService;

@Slf4j
@RestController
@RequestMapping("/currencies")
public class CurrencyController {

    @PostMapping(path = "/wallet")
    public ResponseEntity<IResponse> getCurrenciesForWallet(@Valid @RequestBody GetCurrenciesRequest getCurrenciesRequest) {
        return currencyService.getCurrenciesForWallet(getCurrenciesRequest);
    }

}
