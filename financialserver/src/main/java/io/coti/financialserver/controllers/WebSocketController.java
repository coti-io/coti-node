package io.coti.financialserver.controllers;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.http.WebSocketAuthRequest;
import io.coti.financialserver.services.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingSender;

    @Autowired
    WebSocketService webSocketService;

    @MessageMapping("/disputesSubscribe")
    public ResponseEntity<IResponse> disputesSubscribe(@Payload WebSocketAuthRequest disputeAuthRequest,
                                                       SimpMessageHeaderAccessor headerAccessor) {

        return webSocketService.disputesSubscribe(disputeAuthRequest, headerAccessor);
    }
}
