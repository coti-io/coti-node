package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.GetUnreadEventsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.coti.financialserver.services.NodeServiceManager.eventService;

@Slf4j
@RestController
@RequestMapping("/event")
public class EventController {

    @PostMapping(path = "/unread")
    public ResponseEntity<IResponse> getUnreadEvents(@Valid @RequestBody GetUnreadEventsRequest getUnreadEventsRequest) {

        return eventService.getUnreadEvents(getUnreadEventsRequest);
    }


}
