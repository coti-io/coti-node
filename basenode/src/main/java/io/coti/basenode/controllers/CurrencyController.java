package io.coti.basenode.controllers;

import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.GetUpdatedCurrencyRequest;
import io.coti.basenode.services.BaseNodeCurrencyService;
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
    private BaseNodeCurrencyService baseNodeCurrencyService;

    @PostMapping(path = "/update")
    public ResponseEntity<BaseResponse> getUpdatedCurrencies(@RequestBody @Valid GetUpdatedCurrencyRequest getUpdatedCurrencyRequest) {
        return baseNodeCurrencyService.getUpdatedCurrencies(getUpdatedCurrencyRequest);
    }
}
