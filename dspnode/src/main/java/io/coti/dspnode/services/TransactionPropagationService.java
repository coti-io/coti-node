package io.coti.dspnode.services;

import io.coti.common.communication.MultiClientZeroMQPublisher;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class TransactionPropagationService {
    @Value("${dsp.propagation.address}")
    private String propagationAddress;
    @Autowired
    private MultiClientZeroMQPublisher multiClientPublisher;

    @PostConstruct
    public void init() {
        multiClientPublisher.init(propagationAddress);
    }

    public void propagateSignedTransaction(TransactionData transactionData) {
        multiClientPublisher.sendToAll("SignedTransactions", SerializationUtils.serialize(transactionData));
    }

    @Scheduled(fixedRate = 30000)
    void printStats() {
        log.info("Connected Clients: {}", multiClientPublisher.getConnectedClients());
    }
}
