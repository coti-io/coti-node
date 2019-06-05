package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.FundDistributionRequest;
import io.coti.financialserver.http.GetReservedBalancesRequest;
import io.coti.financialserver.http.TokenSaleDistributionRequest;
import io.coti.financialserver.services.DistributeTokenService;
import io.coti.financialserver.services.FundDistributionService;
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
    FundDistributionService fundDistributionService;

    @PostMapping(path = "/tokensale")
    public ResponseEntity<IResponse> distributeTokenSale(@RequestBody @Valid TokenSaleDistributionRequest request) {
        return distributeTokenService.distributeTokens(request);
    }

    @PostMapping(path = "/funds")
    public ResponseEntity<IResponse> distributeFunds(@Valid @RequestBody FundDistributionRequest request) {
        return fundDistributionService.distributeFundFromFile(request);
    }

    @PostMapping(path = "/funds/manual")
    public ResponseEntity<IResponse> distributeFundsManual(@Valid @RequestBody FundDistributionRequest request) {
        return fundDistributionService.distributeFundFromLocalFile(request);
    }

    @DeleteMapping(path = "/funds/file")
    public ResponseEntity<IResponse> deleteFundFileRecord() {
        return fundDistributionService.deleteFundFileRecord();
    }

    @GetMapping(path = "/balances")
    public ResponseEntity<IResponse> getBalances() {
        return fundDistributionService.getFundBalances();
    }

    @PostMapping(path = "/balance/lockup")
    public ResponseEntity<IResponse> getReservedBalances(@RequestBody @Valid GetReservedBalancesRequest getReservedBalancesRequest) {
        return fundDistributionService.getReservedBalances(getReservedBalancesRequest);
    }
}
