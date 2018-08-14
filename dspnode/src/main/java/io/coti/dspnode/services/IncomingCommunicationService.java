package io.coti.dspnode.services;

import io.coti.common.communication.interfaces.IPropagationSubscriber;
import io.coti.common.communication.interfaces.IReceiver;
import io.coti.common.data.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Service
public class IncomingCommunicationService {
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
        classNameToSubscriberHandlerMapping.put(DspConsensusResult.class.getName() + "Dsp Result", data -> transactionService.handleVoteConclusion((DspConsensusResult) data));
        classNameToSubscriberHandlerMapping.put(TransactionData.class.getName() + ZeroSpendDescription.ZERO_SPEND_TRANSACTION_NO_SOURCE.name(), data
                -> transactionService.handleZeroSpendNoSourceTrx((TransactionData) data));
        classNameToSubscriberHandlerMapping.put(TransactionData.class.getName() + ZeroSpendDescription.ZERO_SPEND_TRANSACTION_STARVATION.name(), data
                -> transactionService.handleZeroSpendStarvationTrx((TransactionData) data));
        propagationSubscriber.init(classNameToSubscriberHandlerMapping);
    }

    private void initReceiver() {
        HashMap<String, Function<Object, String>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(TransactionData.class.getName(), data ->
                transactionService.handleNewTransaction((TransactionData) data));
        classNameToReceiverHandlerMapping.put(AddressData.class.getName(), data ->
                addressService.handleReceivedAddress((AddressData) data));
        classNameToReceiverHandlerMapping.put(ZeroSpendTransactionRequest.class.getName(), data ->
                zeroSpendTransactionService.handleReceivedZeroSpendTransactionRequest((ZeroSpendTransactionRequest) data));
        receiver.init(classNameToReceiverHandlerMapping);
    }
}