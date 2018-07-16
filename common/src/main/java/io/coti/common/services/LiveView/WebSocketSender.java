package io.coti.common.services.LiveView;

import io.coti.common.data.Hash;
import io.coti.common.data.NodeData;
import io.coti.common.http.websocket.UpdatedBalanceMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;

@Slf4j
@Component
public class WebSocketSender {

    private SimpMessagingTemplate messagingSender;

    @PostConstruct
    private void init(){
int x = 5;
    }


    @Autowired
    public WebSocketSender(SimpMessagingTemplate simpMessagingTemplate){
        this.messagingSender = simpMessagingTemplate;
    }

    public void notifyBalanceChange(Hash addressHash, BigDecimal amount) {
        log.trace("Address {} with amount {} is about to be sent to the subscribed user", addressHash, amount);
        messagingSender.convertAndSend("/topic/" + addressHash.toHexString(),
                new UpdatedBalanceMessage(addressHash, amount));
    }

    public void sendNode(NodeData nodeData) {
        messagingSender.convertAndSend("/topic/nodes", nodeData);
    }
}