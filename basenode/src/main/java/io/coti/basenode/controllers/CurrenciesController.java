package io.coti.basenode.controllers;

import io.coti.basenode.http.GetCurrenciesRequest;
import io.coti.basenode.http.GetCurrenciesResponse;
import io.coti.basenode.services.BaseNodeCurrenciesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
public class CurrenciesController {
    @Autowired
    private BaseNodeCurrenciesService baseNodeCurrenciesService;

    @PostMapping(value = "/currencies")
    public ResponseEntity<GetCurrenciesResponse> getMissingCurrencies(@RequestBody @Valid GetCurrenciesRequest getCurrenciesRequest) {
        return baseNodeCurrenciesService.getMissingCurrencies(getCurrenciesRequest);
    }

}
