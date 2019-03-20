package io.coti.dspnode.controllers;

import io.coti.basenode.data.MessageArrivalValidationData;
import io.coti.dspnode.services.MessageArrivalValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
public class ArrivalValidationController {

    @Autowired
    private MessageArrivalValidationService messageArrivalValidationService;


    @PostMapping(value = "/missedDataHashes")
    public ResponseEntity<MessageArrivalValidationData> getMissedDataHashes(@RequestBody MessageArrivalValidationData data) {
        log.info("Received missedDataHashes rest request: {}",data);
        //ResponseEntity<MessageArrivalValidationData> response = new ResponseEntity();
        return ResponseEntity.status(HttpStatus.OK).body(messageArrivalValidationService.getMissedMessageHashes(data));
        //return new ResponseEntity<MessageArrivalValidationData>(messageArrivalValidationService.getMissedMessageHashes(data));
        //return new ResponseEntity<MessageArrivalValidationData>(null);
        //return ResponseEntity.ok(transactionIndexService.isSynchronized(transactionIndexData));
    }

}
