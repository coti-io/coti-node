package io.coti.basenode.controllers;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.http.GetUpdatedCurrencyRequest;
import io.coti.basenode.services.interfaces.ICurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/currencies")
public class BaseNodeCurrencyController {

    @Autowired
    private ICurrencyService currencyService;

    @PostMapping(path = "/update/batch", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<CurrencyData> getUpdatedCurrencyBatch(@RequestBody @Valid GetUpdatedCurrencyRequest getUpdatedCurrencyRequest) {
        return Flux.create(fluxSink ->
                currencyService.getUpdatedCurrencyBatch(getUpdatedCurrencyRequest, fluxSink));
    }

}
