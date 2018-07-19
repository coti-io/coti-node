package io.coti.fullnode.communication;

import io.coti.common.communication.MultiClientZeroMQSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class TransactionsClient {
    @Value("${dsp.propagation.address}")
    private String dspPropagationAddress;

    @Autowired
    private MultiClientZeroMQSubscriber multiClientSubscriber;

    @PostConstruct
    public void init() {
        multiClientSubscriber.subscribe(dspPropagationAddress, message -> log.info(new String(message)));
    }
}