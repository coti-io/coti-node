package io.coti.nodemanager.websocket;

import io.coti.basenode.data.NetworkNodeData;
import io.coti.nodemanager.data.NetworkNodeStatus;
import io.coti.nodemanager.websocket.data.NodeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketSender {

    private final SimpMessagingTemplate messagingSender;

    @Autowired
    public WebSocketSender(SimpMessagingTemplate simpMessagingTemplate) {
        this.messagingSender = simpMessagingTemplate;
    }

    public void notifyNodeDetails(NetworkNodeData networkNodeData, NetworkNodeStatus nodeStatus) {
        log.debug("Node {} with status {} is about to sent to the subscribed users", networkNodeData.getNodeHash(), nodeStatus);
        messagingSender.convertAndSend("/topic/nodes",
                new NodeMessage(networkNodeData, nodeStatus));
        messagingSender.convertAndSend("/topic/node/" + networkNodeData.getNodeHash().toString(),
                new NodeMessage(networkNodeData, nodeStatus));
    }


}
