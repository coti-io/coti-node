package io.coti.zerospend.controllers;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.http.BaseResponse;
import io.coti.zerospend.services.CurrencyService;
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

    @GetMapping(path = "/native")
    public CurrencyData getNativeCurrency() {
        return currencyService.getNativeCurrency();
    }

    @PostMapping(path = "/token")
    public ResponseEntity<BaseResponse> distributeToken(@RequestBody @Valid CurrencyData currencyData) {
        return currencyService.initiateToken(currencyData);
    }

}
