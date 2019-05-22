package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.FundDistributionRequest;
import io.coti.financialserver.http.TokenSaleDistributionRequest;
import io.coti.financialserver.services.DistributeFundService;
import io.coti.financialserver.services.DistributeTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@Slf4j
@RestController
@RequestMapping("/distribution")
public class DistributionController {

    @Autowired
    DistributeTokenService distributeTokenService;
    @Autowired
    DistributeFundService distributeFundService;

    @RequestMapping(path = "/tokensale", method = RequestMethod.POST)
    public ResponseEntity<IResponse> distributeTokenSale(@RequestBody @Valid TokenSaleDistributionRequest request) {
        return distributeTokenService.distributeTokens(request);
    }

    @RequestMapping(path = "/funds", method = RequestMethod.POST)
    public ResponseEntity<IResponse> distributeFunds(@ModelAttribute @Valid FundDistributionRequest request) {
        return distributeFundService.distributeFundFromFile(request);
    }

    @RequestMapping(path = "/balances", method = RequestMethod.GET)
    public ResponseEntity<IResponse> getBalances() {
        return distributeFundService.getFundBalances();
    }
}
