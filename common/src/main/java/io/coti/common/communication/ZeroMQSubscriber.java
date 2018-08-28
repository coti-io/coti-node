package io.coti.common.communication;

import io.coti.common.communication.interfaces.IPropagationSubscriber;
import io.coti.common.communication.interfaces.ISerializer;
import io.coti.common.data.AddressData;
import io.coti.common.data.DspConsensusResult;
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
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket propagationReceiver;

    @Autowired
    private ISerializer serializer;

    private void initSockets(List<String> propagationServerAddresses, List<String> channelsToSubscribe) {
        propagationReceiver = zeroMQContext.socket(ZMQ.SUB);
        propagationReceiver.setHWM(10000);
        ZeroMQUtils.bindToRandomPort(propagationReceiver);
        for (String serverAddress :
                propagationServerAddresses
                ) {
            propagationReceiver.connect(serverAddress);
            for (String channel : channelsToSubscribe) {
                propagationReceiver.subscribe(channel);
            }
        }
    }

    @Override
    public void init(List<String> propagationServerAddresses, HashMap<String, Consumer<Object>> messagesHandler) {
        zeroMQContext = ZMQ.context(1);
        List<String> channelsToSubscribe = new ArrayList<>(messagesHandler.keySet());
        initSockets(propagationServerAddresses, channelsToSubscribe);
        Thread receiverThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                String channel = propagationReceiver.recvStr();
                if (channel.contains(TransactionData.class.getName()) &&
                        messagesHandler.containsKey(channel)) {
                    log.debug("Received a new message on channel: {}", channel);
                    byte[] message = propagationReceiver.recv();
                    try {
                        TransactionData transactionData = serializer.deserialize(message);
                        messagesHandler.get(channel).accept(transactionData);
                    } catch (ClassCastException e) {
                        log.error("Invalid request received: " + e.getMessage());
                    }
                }
                if (channel.contains(AddressData.class.getName()) &&
                        messagesHandler.containsKey(channel)) {
                    log.debug("Received a new message on channel: {}", channel);
                    byte[] message = propagationReceiver.recv();
                    try {
                        AddressData addressData = serializer.deserialize(message);
                        messagesHandler.get(channel).accept(addressData);
                    } catch (ClassCastException e) {
                        log.error("Invalid request received: " + e.getMessage());
                    }
                }
                if (channel.contains(DspConsensusResult.class.getName()) &&
                        messagesHandler.containsKey(channel)) {
                    log.debug("Received a new message on channel: {}", channel);
                    byte[] message = propagationReceiver.recv();
                    try {
                        DspConsensusResult dspConsensusResult = serializer.deserialize(message);
                        messagesHandler.get(channel).accept(dspConsensusResult);
                    } catch (ClassCastException e) {
                        log.error("Invalid request received: " + e.getMessage());
                    }
                }
            }
        });
        receiverThread.start();
    }
}