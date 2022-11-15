package io.coti.basenode.communication;

import io.coti.basenode.communication.interfaces.ISubscriberHandler;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.services.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class ZeroMQSubscriberHandler implements ISubscriberHandler {

    private Map<String, Function<NodeType, Consumer<IPropagatable>>> messageTypeToSubscriberHandlerMap;
    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private IDspVoteService dspVoteService;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private ITransactionHelper transactionHelper;

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
        subscriberMessageType.transactionHelper = transactionHelper;
    }

    @Override
    public Function<NodeType, Consumer<IPropagatable>> get(String messageType) {
        return messageTypeToSubscriberHandlerMap.get(messageType);
    }

}
