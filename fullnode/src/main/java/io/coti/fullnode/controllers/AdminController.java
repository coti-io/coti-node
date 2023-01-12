package io.coti.fullnode.controllers;

import io.coti.basenode.http.RepropagateTransactionByAdminRequest;
import io.coti.basenode.http.interfaces.IResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.fullnode.services.NodeServiceManager.transactionService;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @PostMapping(value = "/transaction/repropagate")
    public ResponseEntity<IResponse> repropagateTransaction(@Valid @RequestBody RepropagateTransactionByAdminRequest repropagateTransactionByAdminRequest) {
        return transactionService.repropagateTransactionByAdmin(repropagateTransactionByAdminRequest);
    }
}
