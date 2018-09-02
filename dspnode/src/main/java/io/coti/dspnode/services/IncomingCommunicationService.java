package io.coti.dspnode.services;


import io.coti.basenode.communication.Channel;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.data.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Service
public class IncomingCommunicationService {
    @Value("#{'${propagation.server.addresses}'.split(',')}")
    private List<String> propagationServerAddress;


    @Value("${receiving.port}")
    private String receivingPort;


    @Autowired
    private IReceiver receiver;
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    @Autowired
    private AddressService addressService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ZeroSpendTransactionService zeroSpendTransactionService;

    @PostConstruct
    public void init() {
        initReceiver();
        initSubscriber();
    }

    private void initSubscriber() {
        HashMap<String, Consumer<Object>> classNameToSubscriberHandlerMapping = new HashMap<>();
        classNameToSubscriberHandlerMapping.put(TransactionData.class.getName() + "DSP Nodes", data -> transactionService.handlePropagatedTransaction((TransactionData) data));
        classNameToSubscriberHandlerMapping.put(AddressData.class.getName() + "DSP Nodes", data -> addressService.handlePropagatedAddress((AddressData) data));
        classNameToSubscriberHandlerMapping.put(DspConsensusResult.class.getName() + "DSP Result", data -> transactionService.handleVoteConclusion((DspConsensusResult) data));
        propagationSubscriber.init(propagationServerAddress,classNameToSubscriberHandlerMapping);
    }

    private void initReceiver() {


        HashMap<String,  Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(Channel.getChannelString(TransactionData.class, NodeType.FullNode), data ->
                transactionService.handleNewTransactionFromFullNode((TransactionData) data));
        classNameToReceiverHandlerMapping.put(Channel.getChannelString(AddressData.class, NodeType.FullNode), data ->
                addressService.handleNewAddressFromFullNode((AddressData) data));

        classNameToReceiverHandlerMapping.put(Channel.getChannelString(ZeroSpendTransactionRequest.class, NodeType.ZeroSpendServer), data ->
                zeroSpendTransactionService.handleReceivedZeroSpendTransactionRequest((ZeroSpendTransactionRequest) data));
       receiver.init(receivingPort,classNameToReceiverHandlerMapping);
    }
}