package io.coti.fullnode.controllers;

import io.coti.basenode.http.GetBalancesRequest;
import io.coti.basenode.http.GetBalancesResponse;
import io.coti.basenode.http.GetTokenBalancesRequest;
import io.coti.basenode.http.GetTokenBalancesResponse;
import io.coti.basenode.services.interfaces.IBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/balance")
public class BalanceController {

    @Autowired
    private IBalanceService balanceService;

    @PostMapping
    public ResponseEntity<GetBalancesResponse> getBalances(@Valid @RequestBody GetBalancesRequest getBalancesRequest) {
        return balanceService.getBalances(getBalancesRequest);
    }

    @PostMapping(value = "/tokens")
    public ResponseEntity<GetTokenBalancesResponse> getTokenBalances(@Valid @RequestBody GetTokenBalancesRequest getTokenBalancesRequest) {
        return balanceService.getTokenBalances(getTokenBalancesRequest);
    }

}