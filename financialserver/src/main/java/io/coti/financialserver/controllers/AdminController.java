package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.AddFundDistributionsRequest;
import io.coti.financialserver.http.GetDistributionsByDateRequest;
import io.coti.financialserver.http.UpdateDistributionAmountRequest;
import io.coti.financialserver.services.FundDistributionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private FundDistributionService fundDistributionService;

    @PostMapping(path = "/distribution/funds/manual")
    public ResponseEntity<IResponse> distributeFundsManual(@Valid @RequestBody AddFundDistributionsRequest request) {
        return fundDistributionService.distributeFundFromLocalFile(request);
    }

    @DeleteMapping(path = "/distribution/funds/file")
    public ResponseEntity<IResponse> deleteFundFileRecord() {
        return fundDistributionService.deleteFundFileRecord();
    }


    @GetMapping(path = "/distribution/funds/failed")
    public ResponseEntity<IResponse> getFailedDistributions() {
        return fundDistributionService.getFailedDistributions();
    }

    @PostMapping(path = "/distribution/funds")
    public ResponseEntity<IResponse> getDistributionsByDate(@Valid @RequestBody GetDistributionsByDateRequest getDistributionsByDateRequest) {
        return fundDistributionService.getDistributionsByDate(getDistributionsByDateRequest);
    }

    @PostMapping(path = "distribution/fund/amount")
    public ResponseEntity<IResponse> updateFundDistributionAmount(@Valid @RequestBody UpdateDistributionAmountRequest updateDistributionAmountRequest) {
        return fundDistributionService.updateFundDistributionAmount(updateDistributionAmountRequest);
    }
}
