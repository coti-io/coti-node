package io.coti.fullnode.controllers;

import io.coti.common.http.GetBalancesRequest;
import io.coti.common.http.GetBalancesResponse;
import io.coti.fullnode.service.interfaces.IBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/balance")
public class BalanceController {

    @Autowired
    private IBalanceService balanceService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<GetBalancesResponse> getBalances(@Valid @RequestBody GetBalancesRequest getBalancesRequest) {

        return balanceService.getBalances(getBalancesRequest);
    }
}