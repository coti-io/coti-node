package io.coti.dspnode.controllers;

import io.coti.basenode.data.TransactionIndexData;
import io.coti.basenode.services.TransactionIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

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