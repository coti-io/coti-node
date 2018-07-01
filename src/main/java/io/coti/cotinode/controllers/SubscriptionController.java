package io.coti.cotinode.controllers;

import io.coti.cotinode.http.websocket.AddressSubscription;
import io.coti.cotinode.http.websocket.BalanceSubscriptionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class SubscriptionController {
    private SimpMessagingTemplate template;

    @Autowired
    public SubscriptionController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @MessageMapping("/subscribe")
    @SendTo("/topic/balance")
    public AddressSubscription subscribeToAddress(BalanceSubscriptionRequest request) {
        return new AddressSubscription("Subscribed to Address: " + request.getAddressHash().toHexString());
    }
}
