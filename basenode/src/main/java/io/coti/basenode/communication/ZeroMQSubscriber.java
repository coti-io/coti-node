package io.coti.basenode.communication;

import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.ISerializer;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@Service
public class ZeroMQSubscriber implements IPropagationSubscriber {
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket propagationReceiver;
    private int port;
    private Map<String, Date> connectedServerAddresses = new ConcurrentHashMap<>();
    private List<String> channelsToSubscribe;
    private final int HEARTBEAT_INTERVAL = 10000;
    private final int INITIAL_DELAY = 5000;
    private final int FIXED_DELAY = 5000;
    @Autowired
    private ISerializer serializer;

    @Override
    public void init(List<String> propagationServerAddresses, HashMap<String, Consumer<Object>> messagesHandler) {
        zeroMQContext = ZMQ.context(1);
        this.channelsToSubscribe = new ArrayList<>(messagesHandler.keySet());
        initSockets(propagationServerAddresses);
        Thread receiverThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                String channel = propagationReceiver.recvStr();
                log.debug("Received a new message on channel: {}", channel);
                byte[] message = propagationReceiver.recv();
                if (channel.contains(TransactionData.class.getName()) &&
                        messagesHandler.containsKey(channel)) {
                    try {
                        TransactionData transactionData = serializer.deserialize(message);
                        messagesHandler.get(channel).accept(transactionData);
                    } catch (ClassCastException e) {
                        log.error("Invalid request received: " + e.getMessage());
                    }
                }
                if (channel.contains(AddressData.class.getName()) &&
                        messagesHandler.containsKey(channel)) {
                    try {
                        AddressData addressData = serializer.deserialize(message);
                        messagesHandler.get(channel).accept(addressData);
                    } catch (ClassCastException e) {
                        log.error("Invalid request received: " + e.getMessage());
                    }
                }
                if (channel.contains(DspConsensusResult.class.getName()) &&
                        messagesHandler.containsKey(channel)) {
                    try {
                        DspConsensusResult dspConsensusResult = serializer.deserialize(message);
                        messagesHandler.get(channel).accept(dspConsensusResult);
                    } catch (ClassCastException e) {
                        log.error("Invalid request received: " + e.getMessage());
                    }
                }
                if (channel.contains("HeartBeat")) {
                    String serverAddress = new String(message);
                    connectedServerAddresses.put(serverAddress, new Date());
                }
            }
        });
        receiverThread.start();
    }

    private void initSockets(List<String> propagationServerAddresses) {
        propagationReceiver = zeroMQContext.socket(ZMQ.SUB);
        propagationReceiver.setHWM(10000);
        port = ZeroMQUtils.bindToRandomPort(propagationReceiver);
        propagationServerAddresses.forEach(serverAddress ->
        {
            if (propagationReceiver.connect(serverAddress)) {
                subscribeAll(serverAddress);
            } else {
                log.error("Unable to connect to server {}", serverAddress);
            }
        });
    }

    private void subscribeAll(String serverAddress) {
        propagationReceiver.subscribe("HeartBeat " + serverAddress);
        channelsToSubscribe.forEach(channel ->
        {
            if (propagationReceiver.subscribe(channel)) {
                log.debug("Subscribed to server {} and channel {}", serverAddress, channel);
            } else {
                log.error("Subscription failed for server {} and channel {}", serverAddress, channel);
            }
        });
    }

    private void unsubscribeAll(String serverAddress) {
        propagationReceiver.unsubscribe("HeartBeat " + serverAddress);
        channelsToSubscribe.forEach(channel ->
        {
            if (propagationReceiver.unsubscribe(channel)) {
                log.debug("Unsubscribed from server {} and channel {}", serverAddress, channel);
            }
        });
    }

    @Scheduled(initialDelay = INITIAL_DELAY, fixedDelay = FIXED_DELAY)
    public void reconnectToPublisher() {
        connectedServerAddresses.forEach((serverAddress, date) -> {
            if (new Date().getTime() - date.getTime() > HEARTBEAT_INTERVAL) {
                log.info("Publisher heartbeat message timeout: server = {}, lastHeartBeat = {}", serverAddress, date);
                unsubscribeAll(serverAddress);
                if (propagationReceiver.connect(serverAddress)) {
                    subscribeAll(serverAddress);
                }
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down ZeroMQ subscriber");
        propagationReceiver.unbind("tcp://*:" + port);
    }
}