package io.coti.basenode.controllers;

import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.GetCurrencyRequest;
import io.coti.basenode.http.GetUpdatedCurrencyRequest;
import io.coti.basenode.services.BaseNodeCurrencyService;
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
    private BaseNodeCurrencyService baseNodeCurrencyService;

    @PostMapping()
    public ResponseEntity<BaseResponse> getMissingCurrencies(@RequestBody @Valid GetCurrencyRequest getCurrencyRequest) {
        return baseNodeCurrencyService.getMissingCurrencies(getCurrencyRequest);
    }

    @PostMapping(path = "/update")
    public ResponseEntity<BaseResponse> getUpdatedCurrencies(@RequestBody @Valid GetUpdatedCurrencyRequest getUpdatedCurrencyRequest) {
        return baseNodeCurrencyService.getUpdatedCurrencies(getUpdatedCurrencyRequest);
    }

    @GetMapping(path = "/native")
    public ResponseEntity<BaseResponse> getMissingNativeCurrency() {
        return baseNodeCurrencyService.getMissingNativeCurrency();
    }
}
