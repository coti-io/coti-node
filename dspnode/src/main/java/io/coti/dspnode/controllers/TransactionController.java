package io.coti.dspnode.controllers;

import io.coti.common.crypto.NodeCryptoHelper;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionIndexData;
import io.coti.common.http.AddressRequest;
import io.coti.common.http.BaseResponse;
import io.coti.common.services.TransactionIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@Controller
public class TransactionController {
    @Autowired
    private TransactionIndexService transactionIndexService;

    @RequestMapping(value = "/isSynchronized", method = POST)
    public ResponseEntity<Boolean> isSynchronized(@Valid @RequestBody TransactionIndexData transactionIndexData) {
            return ResponseEntity.ok(transactionIndexService.isSynchronized(transactionIndexData));
    }
}