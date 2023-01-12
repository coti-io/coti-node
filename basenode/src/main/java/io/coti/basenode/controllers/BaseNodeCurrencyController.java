package io.coti.basenode.controllers;

import io.coti.basenode.http.GetTokenDetailsRequest;
import io.coti.basenode.http.GetTokenHistoryRequest;
import io.coti.basenode.http.GetTokenSymbolDetailsRequest;
import io.coti.basenode.http.GetUserTokensRequest;
import io.coti.basenode.http.interfaces.IResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.basenode.services.BaseNodeServiceManager.currencyService;

@Slf4j
@RestController
@RequestMapping("/currencies")
public class BaseNodeCurrencyController {

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

    @PostMapping(path = "/token/history")
    public ResponseEntity<IResponse> getTokenHistory(@Valid @RequestBody GetTokenHistoryRequest getTokenHistoryRequest) {
        return currencyService.getTokenHistory(getTokenHistoryRequest);
    }
}
