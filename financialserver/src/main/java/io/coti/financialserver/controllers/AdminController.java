package io.coti.financialserver.controllers;

import io.coti.basenode.http.GenerateTokenFeeRequest;
import io.coti.basenode.http.GetTokenMintingFeeQuoteRequest;
import io.coti.basenode.http.TokenMintingFeeRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.AddFundDistributionsRequest;
import io.coti.financialserver.http.GetDistributionsByDateRequest;
import io.coti.financialserver.http.UpdateDistributionAmountRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static io.coti.financialserver.services.NodeServiceManager.*;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

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

    @PostMapping(path = "/token/generate")
    public ResponseEntity<IResponse> getTokenGenerationFee(@Valid @RequestBody GenerateTokenFeeRequest generateTokenFeeRequest) {
        return currencyService.getTokenGenerationFee(generateTokenFeeRequest);
    }

    @PostMapping(path = "/token/mint/quote")
    public ResponseEntity<IResponse> getTokenMintingFeeQuote(@Valid @RequestBody GetTokenMintingFeeQuoteRequest getTokenMintingFeeQuoteRequest) {
        return mintingService.getTokenMintingFeeQuote(getTokenMintingFeeQuoteRequest);
    }

    @PostMapping(path = "/token/mint/fee")
    public ResponseEntity<IResponse> getTokenMintingFee(@Valid @RequestBody TokenMintingFeeRequest tokenMintingFeeRequest) {
        return mintingService.getTokenMintingFee(tokenMintingFeeRequest);
    }

}
