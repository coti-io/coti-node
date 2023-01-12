package io.coti.fullnode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.fullnode.http.FullNodeFeeRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.fullnode.services.NodeServiceManager.feeService;

@RequestMapping("/fee")
@RestController
public class FeeController {

    @PutMapping()
    public ResponseEntity<IResponse> createFullNodeFee(@Valid @RequestBody FullNodeFeeRequest fullNodeFeeRequest) {
        return feeService.createFullNodeFee(fullNodeFeeRequest);
    }
}
