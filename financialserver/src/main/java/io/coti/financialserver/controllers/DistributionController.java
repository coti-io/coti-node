package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.TokenSaleDistributionRequest;
import io.coti.financialserver.services.DistributeTokensService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Slf4j
@RestController
public class DistributionController {

    @Autowired
    DistributeTokensService distributeTokensService;

    @RequestMapping(path = "/distributeTokenSale", method = RequestMethod.POST)
    public ResponseEntity<IResponse> distributeTokenSale(@RequestBody @Valid TokenSaleDistributionRequest request) {
        return distributeTokensService.distributeTokens(request);

    }
}
