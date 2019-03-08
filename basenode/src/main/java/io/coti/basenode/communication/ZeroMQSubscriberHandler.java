package io.coti.basenode.communication;

import io.coti.basenode.communication.interfaces.ISubscriberHandler;
import io.coti.basenode.data.NodeType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class ZeroMQSubscriberHandler implements ISubscriberHandler {
    private Map<String, Function<NodeType, Consumer<Object>>> messageTypeToSubscriberHandlerMap;

    @PostConstruct
    public void init() {
        messageTypeToSubscriberHandlerMap = new HashMap<>();
        EnumSet.allOf(SubscriberMessageType.class).forEach(subscriberMessageType -> messageTypeToSubscriberHandlerMap.put(subscriberMessageType.toString(), publisherNodeType -> subscriberMessageType.getHandler(publisherNodeType)));
    }

    @Override
    public Function<NodeType, Consumer<Object>> get(String messageType) {
        return messageTypeToSubscriberHandlerMap.get(messageType);
    }

}
