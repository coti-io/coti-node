package io.coti.cotinode.http.websocket;

import io.coti.cotinode.data.Hash;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class WebSocketSender {
    @Autowired
    private SimpMessagingTemplate messagingSender;

    public void notifyBalanceChange(Hash addressHash, BigDecimal amount){
        messagingSender.convertAndSend("/topic/" + addressHash.toHexString(),
                new UpdatedBalanceMessage(addressHash, amount));
    }
}
