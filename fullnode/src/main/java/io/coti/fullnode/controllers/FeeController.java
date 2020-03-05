package io.coti.fullnode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.fullnode.http.FullNodeFeeRequest;
import io.coti.fullnode.services.FeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequestMapping("/fee")
@RestController
public class FeeController {

    @Autowired
    private FeeService feeService;

    @PutMapping()
    public ResponseEntity<IResponse> createFullNodeFee(@Valid @RequestBody FullNodeFeeRequest fullNodeFeeRequest) {
        return feeService.createFullNodeFee(fullNodeFeeRequest);
    }
}
