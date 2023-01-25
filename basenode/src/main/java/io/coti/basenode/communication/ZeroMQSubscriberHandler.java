package io.coti.basenode.communication;

import io.coti.basenode.communication.interfaces.ISubscriberHandler;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class ZeroMQSubscriberHandler implements ISubscriberHandler {

    private Map<String, Function<NodeType, Consumer<IPropagatable>>> messageTypeToSubscriberHandlerMap;

    @Override
    public void init() {
        messageTypeToSubscriberHandlerMap = new HashMap<>();
        EnumSet.allOf(SubscriberMessageType.class).forEach(subscriberMessageType ->
                messageTypeToSubscriberHandlerMap.put(subscriberMessageType.getMessageTypeClass().getSimpleName(), subscriberMessageType::getHandler));
    }

    @Override
    public Function<NodeType, Consumer<IPropagatable>> get(String messageType) {
        return messageTypeToSubscriberHandlerMap.get(messageType);
    }

}
