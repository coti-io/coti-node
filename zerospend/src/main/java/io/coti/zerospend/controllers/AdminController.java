package io.coti.zerospend.controllers;

import io.coti.basenode.data.Event;
import io.coti.basenode.http.SetIndexesRequest;
import io.coti.basenode.http.interfaces.IResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static io.coti.zerospend.services.NodeServiceManager.transactionCreationService;
import static io.coti.zerospend.services.NodeServiceManager.transactionService;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @PutMapping(path = "/transaction/index")
    public ResponseEntity<IResponse> setIndexToTransactions(@RequestBody SetIndexesRequest setIndexesRequest) {
        return transactionService.setIndexToTransactions(setIndexesRequest);
    }

    @PostMapping(path = "/event/multi-dag")
    public ResponseEntity<IResponse> eventMultiDag() {
        return transactionCreationService.createEventTransaction("Multi DAG", Event.MULTI_DAG
        );
    }

}
