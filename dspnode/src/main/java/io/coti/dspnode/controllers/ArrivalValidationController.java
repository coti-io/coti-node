package io.coti.dspnode.controllers;

import io.coti.basenode.data.MessageArrivalValidationData;
import io.coti.dspnode.services.MessageArrivalValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@Controller
public class ArrivalValidationController {

    @Autowired
    private MessageArrivalValidationService messageArrivalValidationService;

    @PostMapping(value = "/missedDataHashes")
    public ResponseEntity<MessageArrivalValidationResponse> getMissedDataHashes(@RequestBody MessageArrivalValidationData data) {
        log.info("Received missedDataHashes request");
        MessageArrivalValidationData responseData = messageArrivalValidationService.getMissedMessageHashes(data);
        MessageArrivalValidationResponse response;
        if(response != null){
            return ResponseEntity.status(HttpStatus.OK).body(me ssageArrivalValidationService.getMissedMessageHashes(data));
        }
        else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
