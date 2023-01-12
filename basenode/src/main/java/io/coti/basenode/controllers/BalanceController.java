package io.coti.basenode.controllers;

import io.coti.basenode.http.GetBalancesRequest;
import io.coti.basenode.http.GetBalancesResponse;
import io.coti.basenode.http.GetTokenBalancesRequest;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.basenode.services.BaseNodeServiceManager.balanceService;

@RestController
@RequestMapping("/balance")
public class BalanceController {

    @PostMapping()
    public ResponseEntity<GetBalancesResponse> getBalances(@Valid @RequestBody GetBalancesRequest getBalancesRequest) {
        return balanceService.getBalances(getBalancesRequest);
    }

    @PostMapping(value = "/tokens")
    public ResponseEntity<IResponse> getTokenBalances(@Valid @RequestBody GetTokenBalancesRequest getTokenBalancesRequest) {
        return balanceService.getTokenBalances(getTokenBalancesRequest);
    }
}
