package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.DisputeEventReadRequest;
import io.coti.financialserver.services.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class WebSocketController {

    @Autowired
    private WebSocketService webSocketService;

    @MessageMapping("/eventRead")
    public ResponseEntity<IResponse> eventRead(@Payload DisputeEventReadRequest disputeEventReadRequest) {

        return webSocketService.eventRead(disputeEventReadRequest);
    }

}
