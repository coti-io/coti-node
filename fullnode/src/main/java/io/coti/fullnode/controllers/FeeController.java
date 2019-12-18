package io.coti.fullnode.controllers;

import io.coti.basenode.http.BaseResponse;
import io.coti.fullnode.http.FullNodeFeeRequest;
import io.coti.fullnode.services.FeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RequestMapping("/fee")
@RestController
@Slf4j
public class FeeController {
    @Autowired
    private FeeService feeService;

    @PutMapping()
    public ResponseEntity<BaseResponse> createFullNodeFee(@Valid @RequestBody FullNodeFeeRequest fullNodeFeeRequest) {
        return feeService.createFullNodeFee(fullNodeFeeRequest);
    }
}
