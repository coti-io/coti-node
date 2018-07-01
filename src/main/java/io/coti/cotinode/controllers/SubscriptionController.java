package io.coti.cotinode.controllers;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.http.websocket.AddressSubscription;
import io.coti.cotinode.http.websocket.BalanceSubscriptionRequest;
import io.coti.cotinode.http.websocket.UpdatedBalanceMessage;
import io.coti.cotinode.service.BalanceSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Controller
public class SubscriptionController {
    private SimpMessagingTemplate template;

    @Autowired
    public SubscriptionController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @Autowired
    BalanceSubscriptionService balanceSubscriptionService;

    @MessageMapping("/subscribe")
    @SendTo("/topic/balance")
    public AddressSubscription subscribeToAddress(BalanceSubscriptionRequest request) {
        balanceSubscriptionService.subscribe(request.getAddressHash(), this);
        return new AddressSubscription("Subscribed to Address: " +request.getAddressHash().toHexString());
    }

    public void sendBalanceUpdate(Hash addressHash, double balance) {
        this.template.convertAndSend("/topic/" + addressHash,
                new UpdatedBalanceMessage(addressHash, balance));
    }
}
