package io.coti.zerospend.controllers;

import io.coti.basenode.data.CurrencyData;
import io.coti.zerospend.services.CurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
