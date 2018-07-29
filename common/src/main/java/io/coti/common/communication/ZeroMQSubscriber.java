package io.coti.common.communication;

import io.coti.common.communication.interfaces.IPropagationSubscriber;
import io.coti.common.communication.interfaces.ISerializer;
import io.coti.common.data.AddressData;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
public class ZeroMQSubscriber implements IPropagationSubscriber {
    @Value("#{'${propagation.server.addresses}'.split(',')}")
    private List<String> propagationServerAddresses;
    private HashMap<String, Consumer<Object>> messagesHandler;

    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket propagationReceiver;

    @Autowired
    private ISerializer serializer;

    private void initSockets(List<String> channelsToSubscribe) {
        propagationReceiver = zeroMQContext.socket(ZMQ.SUB);
        ZeroMQUtils.bindToRandomPort(propagationReceiver);
        for (String serverAddress :
                propagationServerAddresses
                ) {
            propagationReceiver.connect(serverAddress);
            for(String channel : channelsToSubscribe) {
                propagationReceiver.subscribe(channel);
            }
        }
    }

    @Override
    public void init(HashMap<String, Consumer<Object>> messagesHandler) {
        zeroMQContext = ZMQ.context(1);
        List<String> channelsToSubscribe = new ArrayList<>(messagesHandler.keySet());
        initSockets(channelsToSubscribe);
        Thread receiverThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                String channel = propagationReceiver.recvStr();
                if(channel.contains(TransactionData.class.getName()) &&
                        messagesHandler.containsKey(channel)){
                    log.info("Received a new message on channel: {}", channel);
                    byte[] message = propagationReceiver.recv();
                    TransactionData transactionData = serializer.deserializeTransaction(message);
                    messagesHandler.get(channel).accept(transactionData);
                }
                if(channel.contains(AddressData.class.getName()) &&
                        messagesHandler.containsKey(channel)){
                     log.info("Received a new message on channel: {}", channel);
                    byte[] message = propagationReceiver.recv();
                    AddressData addressData = serializer.deserializeAddress(message);
                    messagesHandler.get(channel).accept(addressData);
                }
            }
        });
        receiverThread.start();
    }
}