package io.coti.basenode.controllers;

import io.coti.basenode.data.Event;
import io.coti.basenode.http.interfaces.IResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.coti.basenode.services.BaseNodeServiceManager.nodeEventService;

@Slf4j
@RestController
@RequestMapping("/event")
public class BaseNodeEventController {

    @GetMapping(path = "/multi-dag/confirmed")
    public ResponseEntity<IResponse> getConfirmedMultiDagEvent() {
        return nodeEventService.getConfirmedEventTransactionDataResponse(Event.MULTI_DAG);
    }

    @GetMapping(path = "/multi-dag")
    public ResponseEntity<IResponse> getMultiDagEventTransactionData() {
        return nodeEventService.getEventTransactionDataResponse(Event.MULTI_DAG);
    }
}
