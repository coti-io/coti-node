package io.coti.basenode.controllers;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.GetUpdatedCurrencyRequest;
import io.coti.basenode.http.data.GetHashToPropagatable;
import io.coti.basenode.services.BaseNodeCurrencyService;
import io.coti.basenode.services.interfaces.ICurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/currencies")
public class CurrencyController {
    @Autowired
    private ICurrencyService currencyService;

    @PostMapping(path = "/update")
    public ResponseEntity<BaseResponse> getUpdatedCurrencies(@RequestBody @Valid GetUpdatedCurrencyRequest getUpdatedCurrencyRequest) {
        return currencyService.getUpdatedCurrencies(getUpdatedCurrencyRequest);
    }

    @PostMapping(path = "/update/reactive", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<CurrencyData> getUpdatedCurrenciesReactive(@RequestBody @Valid GetUpdatedCurrencyRequest getUpdatedCurrencyRequest) {
        return Flux.create(fluxSink ->
                currencyService.getUpdatedCurrenciesReactive(getUpdatedCurrencyRequest, fluxSink));
    }


}
