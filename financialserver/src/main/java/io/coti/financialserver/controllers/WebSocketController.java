package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.DisputeEventReadRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import static io.coti.financialserver.services.NodeServiceManager.webSocketService;

@Slf4j
@Controller
public class WebSocketController {

    @MessageMapping("/eventRead")
    public ResponseEntity<IResponse> eventRead(@Payload DisputeEventReadRequest disputeEventReadRequest) {

        return webSocketService.eventRead(disputeEventReadRequest);
    }

}
