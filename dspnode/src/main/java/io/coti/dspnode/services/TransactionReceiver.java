package io.coti.dspnode.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.common.communication.ZeroMQDirectConnectionServer;
import io.coti.common.data.TransactionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.function.Consumer;

@Service
public class TransactionReceiver {
    @Autowired
    private ZeroMQDirectConnectionServer zeroMQDirectConnectionServer;
    @Value("${dsp.receiver.address}")
    private String receiverAddress;
    private Consumer<TransactionData> transactionDataHandler;

    public void init(Consumer<TransactionData> transactionDataHandler){
        this.transactionDataHandler = transactionDataHandler;
        zeroMQDirectConnectionServer.init(receiverAddress);
        Thread requestReceiverThread = new Thread(() -> {
            zeroMQDirectConnectionServer.runReceiveLoop(messageHandler);
        });
        requestReceiverThread.start();
    }

    private Consumer<byte[]> messageHandler = bytes -> {
        ObjectMapper serializer = new ObjectMapper();
        serializer.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        TransactionData transactionData = null;
        try {
            transactionData = serializer.readValue(bytes, TransactionData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        transactionDataHandler.accept(transactionData);
    };
}
