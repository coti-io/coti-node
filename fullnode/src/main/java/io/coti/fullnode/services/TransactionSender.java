package io.coti.fullnode.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class TransactionSender {
    @Value("${fullnode.sender.address}")
    private String senderAddress;
    @Value("${dsp.receiver.address}")
    private String dspReceiverAddress;

    @Autowired
    private ZeroMQDirectConnectionClient zeroMQDirectConnectionClient;

    @PostConstruct
    public void init() {
        zeroMQDirectConnectionClient.init(senderAddress, dspReceiverAddress);
    }

    public void sendTransaction(TransactionData transactionData) throws JsonProcessingException {
        ObjectMapper serializer = new ObjectMapper();
        zeroMQDirectConnectionClient.send(serializer.writeValueAsBytes(transactionData));
    }
}
