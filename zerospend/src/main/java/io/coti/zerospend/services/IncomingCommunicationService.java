package io.coti.zerospend.services;

import io.coti.basenode.communication.Channel;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;

import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.data.*;
import io.coti.basenode.services.interfaces.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class IncomingCommunicationService {


    @Value("#{'${dsp.server.addresses}'.split(',')}")
    private List<String> propagationDspAddresses;


    @Value("${receiving.port}")
    private String receivingPort;

    @Autowired
    private IReceiver zeroMQTransactionReceiver;
    @Autowired
    private ZeroSpendTransactionCreationService zeroSpendTransactionCreationService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private DspVoteService dspVoteService;


    @PostConstruct
    private void init() {
        initZeroSpendTransactionRequestReceiver();
        initPropagationSubscriber();
    }

    private void initPropagationSubscriber() {


        HashMap<String, Consumer<Object>> classNameToSubscriberHandlerMapping = new HashMap<>();
        classNameToSubscriberHandlerMapping.put(Channel.getChannelString(TransactionData.class, NodeType.DspNode), transactionData ->
                transactionService.handlePropagatedTransaction((TransactionData) transactionData));
        classNameToSubscriberHandlerMapping.put(Channel.getChannelString(AddressData.class, NodeType.DspNode), data ->
                addressService.handlePropagatedAddress((AddressData) data));
        propagationSubscriber.init(propagationDspAddresses, classNameToSubscriberHandlerMapping);
    }



    private void initZeroSpendTransactionRequestReceiver() {
        HashMap<String,Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(Channel.getChannelString(DspVote.class, NodeType.DspNode) , dspVote -> dspVoteService.receiveDspVote((DspVote) dspVote));
        classNameToReceiverHandlerMapping.put(Channel.getChannelString(ZeroSpendTransactionRequest.class, NodeType.ZeroSpendServer) , newZeroSpendTransactionRequest -> zeroSpendTransactionCreationService.createNewGenesisZeroSpendTransaction((ZeroSpendTransactionRequest) newZeroSpendTransactionRequest));
        zeroMQTransactionReceiver.init(receivingPort, classNameToReceiverHandlerMapping);
    }
}