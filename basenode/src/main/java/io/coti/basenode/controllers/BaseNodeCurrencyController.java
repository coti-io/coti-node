package io.coti.basenode.controllers;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.http.*;
import io.coti.basenode.http.interfaces.IResponse;
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
public class BaseNodeCurrencyController {

    @Autowired
    private ICurrencyService currencyService;

    @PostMapping(path = "/token/user")
    public ResponseEntity<IResponse> getUserTokens(@Valid @RequestBody GetUserTokensRequest getUserTokensRequest) {
        return currencyService.getUserTokens(getUserTokensRequest);
    }

    @PostMapping(path = "/token/details")
    public ResponseEntity<IResponse> getTokenDetails(@Valid @RequestBody GetTokenDetailsRequest getTokenDetailsRequest) {
        return currencyService.getTokenDetails(getTokenDetailsRequest);
    }

    @PostMapping(path = "/token/symbol/details")
    public ResponseEntity<IResponse> getSymbolDetails(@Valid @RequestBody GetTokenSymbolDetailsRequest getTokenSymbolDetailsRequest) {
        return currencyService.getTokenSymbolDetails(getTokenSymbolDetailsRequest);
    }

    @PostMapping(path = "/update/batch", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<CurrencyData> getUpdatedCurrencyBatch(@RequestBody @Valid SerializableRequest getUpdatedCurrencyRequest) {
        return Flux.create(fluxSink ->
                currencyService.getUpdatedCurrencyBatch(fluxSink));
    }

}
