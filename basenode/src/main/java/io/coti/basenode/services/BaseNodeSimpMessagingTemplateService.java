package io.coti.basenode.services;


import io.coti.basenode.data.WebSocketData;
import io.coti.basenode.services.interfaces.ISimpleMessagingTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
public class BaseNodeSimpMessagingTemplateService implements ISimpleMessagingTemplate {

    private final SimpMessagingTemplate messagingSender;
    private static BlockingQueue<WebSocketData> queue = new LinkedBlockingQueue<>();
    private static Thread senderQueueThread = null;

    @Autowired
    public BaseNodeSimpMessagingTemplateService(SimpMessagingTemplate messagingSender) {
        this.messagingSender = messagingSender;
        senderQueueThread = new Thread(() -> convertAndSend(queue));
        senderQueueThread.start();
    }

    @Override
    public void convertAndSend(String address, Object obj) {
        WebSocketData webSocketData = new WebSocketData(obj, address);
        try {
            queue.put(webSocketData);
        } catch (InterruptedException e) {
            log.error("Interrupted - Web Socket Queue Full!");
        }
    }

    private void convertAndSend(BlockingQueue<WebSocketData> queue) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                WebSocketData senderData = queue.take();
                String address = senderData.getAddress();
                Object toSend = senderData.getData();
                messagingSender.convertAndSend(address, toSend);
            } catch (InterruptedException e) {
                log.info("Interrupted - stopped taking data from Sender Data Queue");
            }
        }
    }
}
