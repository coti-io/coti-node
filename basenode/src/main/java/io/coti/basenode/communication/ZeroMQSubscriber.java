package io.coti.basenode.communication;

import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.ISerializer;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.Network;
import io.coti.basenode.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    List<String> channelsToSubscribe;
    Thread receiverThread;

    @Autowired
    private ISerializer serializer;

    private void initSocket() {
        propagationReceiver = zeroMQContext.socket(ZMQ.SUB);
        propagationReceiver.setHWM(10000);
        ZeroMQUtils.bindToRandomPort(propagationReceiver);
    }

    @Override
    public void init(HashMap<String, Consumer<Object>> messagesHandler) {
        zeroMQContext = ZMQ.context(1);
        channelsToSubscribe = new ArrayList<>(messagesHandler.keySet());
        initSocket();
        receiverThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                String channel = propagationReceiver.recvStr();
                log.debug("Received a new message on channel: {}", channel);
                if (channel.contains(TransactionData.class.getName()) &&
                        messagesHandler.containsKey(channel)) {
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
                    byte[] message = propagationReceiver.recv();
                    try {
                        DspConsensusResult dspConsensusResult = serializer.deserialize(message);
                        messagesHandler.get(channel).accept(dspConsensusResult);
                    } catch (ClassCastException e) {
                        log.error("Invalid request received: " + e.getMessage());
                    }
                }
                if (channel.contains(Network.class.getName()) &&
                        messagesHandler.containsKey(channel)) {
                    byte[] message = propagationReceiver.recv();
                    try {
                        Network network = serializer.deserialize(message);
                        messagesHandler.get(channel).accept(network);
                    } catch (ClassCastException e) {
                        log.error("Invalid request received: " + e.getMessage());
                    }
                }
            }
        });
        receiverThread.start();
    }

    public void addAddress(String propagationAddressAndPort){
        log.info("ZeroMQ subscriber connecting to address {}", propagationAddressAndPort);
        propagationReceiver.connect(propagationAddressAndPort);
    }

    public void subscribeToChannels(){
        for (String channel : channelsToSubscribe) {
            propagationReceiver.subscribe(channel);
            log.info("Adding propagation subscription at channel {}", channel);
        }
    }

    public void addAddress(String propagationServerAddress, String propagationServerPort) {
        addAddress("tcp://" + propagationServerAddress + ":" + propagationServerPort);
    }
}