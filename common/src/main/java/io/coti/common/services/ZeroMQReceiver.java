package io.coti.common.services;

import io.coti.common.communication.DspVote;
import io.coti.common.communication.interfaces.IReceiver;
import io.coti.common.communication.interfaces.ISerializer;
import io.coti.common.data.AddressData;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.function.Function;

@Slf4j
@Service
public class ZeroMQReceiver implements IReceiver {
    private HashMap<String, Function<Object, String>> classNameToHandlerMapping;

    @Value("${transaction.receiving.port}")
    private String transactionReceivingPort;

    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket receiver;

    @Autowired
    private ISerializer serializer;

    @Override
    public void init(HashMap<String, Function<Object, String>> classNameToHandlerMapping) {
        this.classNameToHandlerMapping = classNameToHandlerMapping;
        zeroMQContext = ZMQ.context(1);
        receiver = zeroMQContext.socket(ZMQ.ROUTER);
        receiver.bind("tcp://*:" + transactionReceivingPort);
        log.info("Zero MQ Client Connected!");
        Thread receiverThread = new Thread(() -> {
            runReceiveLoop();
        });
        receiverThread.start();
    }

    private void runReceiveLoop() {
        while (true) {
            String classType = receiver.recvStr();
            if (classType.equals(TransactionData.class.getName()) &&
                    classNameToHandlerMapping.containsKey(classType)) {
                log.info("Received a new Transaction...");
                byte[] message = receiver.recv();
                try {
                    TransactionData transactionData = serializer.deserialize(message);
                    String answer = classNameToHandlerMapping.get(classType).apply(transactionData);
                } catch (ClassCastException e) {
                    log.error("Invalid request received: " + e.getMessage());
                }
            }
            if (classType.equals(AddressData.class.getName()) &&
                    classNameToHandlerMapping.containsKey(classType)) {
                log.info("Received a new Address...");
                byte[] message = receiver.recv();
                try {
                    AddressData addressData = serializer.deserialize(message);
                    String answer = classNameToHandlerMapping.get(classType).apply(addressData);
                } catch (ClassCastException e) {
                    log.error("Invalid request received: " + e.getMessage());
                }
            }
            if (classType.equals(DspVote.class.getName()) &&
                    classNameToHandlerMapping.containsKey(classType)) {
                log.info("Received a new Address...");
                byte[] message = receiver.recv();
                try {
                    DspVote dspVote = serializer.deserialize(message);
                    String answer = classNameToHandlerMapping.get(classType).apply(dspVote);
                } catch (ClassCastException e) {
                    log.error("Invalid request received: " + e.getMessage());
                }
            }
        }
    }
}
