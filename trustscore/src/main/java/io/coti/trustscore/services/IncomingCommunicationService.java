package io.coti.trustscore.services;

import io.coti.common.communication.interfaces.IPropagationSubscriber;
import io.coti.common.data.AddressData;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.function.Consumer;

@Slf4j
@Service
public class IncomingCommunicationService {
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    @Autowired
    private AddressService addressService;
    @Autowired
    private TransactionService transactionService;

    @PostConstruct
    public void init() {
        initSubscriber();
    }

    private void initSubscriber() {
        HashMap<String, Consumer<Object>> classNameToSubscriberHandlerMapping = new HashMap<>();
        classNameToSubscriberHandlerMapping.put(TransactionData.class.getName() + "TrustScore Nodes", data
                -> transactionService.handlePropagatedTransaction((TransactionData) data));
        classNameToSubscriberHandlerMapping.put(AddressData.class.getName() + "TrustScore Nodes", data
                -> addressService.handlePropagatedAddress((AddressData) data));
        propagationSubscriber.init(classNameToSubscriberHandlerMapping);
    }
}