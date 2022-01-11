package io.coti.zerospend.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeEventService;
import io.coti.basenode.services.interfaces.IEventService;
import io.coti.zerospend.http.SetIndexesRequest;
import io.coti.zerospend.services.TransactionCreationService;
import io.coti.zerospend.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionCreationService transactionCreationService;
    @Autowired
    private IEventService eventService;

    @PutMapping(path = "/transaction/index")
    public ResponseEntity<IResponse> setIndexToTransactions(@RequestBody SetIndexesRequest setIndexesRequest) {
        return transactionService.setIndexToTransactions(setIndexesRequest);
    }

    @PostMapping(path = "/event/multi-currency")
    public ResponseEntity<IResponse> eventMultiCurrency() {
        return transactionCreationService.createEventTransaction("Multi Currencies Support via DAG", BaseNodeEventService.EVENTS.MULTI_CURRENCY,
                true);
    }

}
