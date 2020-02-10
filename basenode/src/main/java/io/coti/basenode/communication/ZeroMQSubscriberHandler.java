package io.coti.basenode.communication;

import io.coti.basenode.communication.interfaces.ISubscriberHandler;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.interfaces.IAddressService;
import io.coti.basenode.services.interfaces.IDspVoteService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class ZeroMQSubscriberHandler implements ISubscriberHandler {

    private Map<String, Function<NodeType, Consumer<Object>>> messageTypeToSubscriberHandlerMap;
    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private IDspVoteService dspVoteService;
    @Autowired
    private INetworkService networkService;

    @Override
    public void init() {
        messageTypeToSubscriberHandlerMap = new HashMap<>();
        EnumSet.allOf(SubscriberMessageType.class).forEach(subscriberMessageType -> {
            injectSubscriberMessageHandleServices(subscriberMessageType);
            messageTypeToSubscriberHandlerMap.put(subscriberMessageType.getMessageTypeClass().getSimpleName(), subscriberMessageType::getHandler);
        });
    }

    private void injectSubscriberMessageHandleServices(SubscriberMessageType subscriberMessageType) {
        subscriberMessageType.transactionService = transactionService;
        subscriberMessageType.addressService = addressService;
        subscriberMessageType.dspVoteService = dspVoteService;
        subscriberMessageType.networkService = networkService;
    }

    @Override
    public Function<NodeType, Consumer<Object>> get(String messageType) {
        return messageTypeToSubscriberHandlerMap.get(messageType);
    }

}
