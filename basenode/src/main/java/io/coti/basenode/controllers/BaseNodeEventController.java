package io.coti.basenode.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeEventService;
import io.coti.basenode.services.interfaces.IEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/event")
public class BaseNodeEventController {

    @Autowired
    IEventService eventService;

    @GetMapping(path = "/multi-currency/confirmed")
    public ResponseEntity<IResponse> getMultiCurrencyEvent() {
        return eventService.getConfirmedEventTransactionDataResponse(BaseNodeEventService.EVENTS.MULTI_CURRENCY);
    }

    @GetMapping(path = "/multi-currency")
    public ResponseEntity<IResponse> getMultiCurrencyEventTransactionData() {
        return eventService.getEventTransactionDataResponse(BaseNodeEventService.EVENTS.MULTI_CURRENCY);
    }
}
