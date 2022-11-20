package io.coti.nodemanager.websocket;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.BaseNodeMonitorService;
import io.coti.nodemanager.data.NetworkNodeStatus;
import io.coti.nodemanager.websocket.data.NodeMessage;
import io.coti.nodemanager.websocket.data.NotifyNodeHealthStateChange;
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

    public void notifyNodeHealthState(Hash nodeHash, BaseNodeMonitorService.HealthState healthState) {
        log.debug("Node {} is now reported with health state {} ", nodeHash, healthState);
        messagingSender.convertAndSend("/topic/nodes/health/",
                new NotifyNodeHealthStateChange(nodeHash, healthState));
        messagingSender.convertAndSend("/topic/node/health/" + nodeHash.toString(),
                new NotifyNodeHealthStateChange(nodeHash, healthState));
    }

}
