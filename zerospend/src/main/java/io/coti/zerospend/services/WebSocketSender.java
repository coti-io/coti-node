package io.coti.zerospend.services;

import io.coti.basenode.services.interfaces.IZeroSpendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class WebSocketSender {


    @Autowired
    private IZeroSpendService zeroSpendService;

    private SimpMessagingTemplate messagingSender;

    @Autowired
    public WebSocketSender(SimpMessagingTemplate simpMessagingTemplate) {
        this.messagingSender = simpMessagingTemplate;
    }

    public void sendGenesisTransactions() {
        messagingSender.convertAndSend("/topic/getGenesisTransactions",
                zeroSpendService.getGenesisTransactions());
    }


}
