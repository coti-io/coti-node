package io.coti.zerospend.services;

import io.coti.common.communication.interfaces.IPropagationSubscriber;
import io.coti.common.communication.interfaces.IReceiver;
import io.coti.common.data.AddressData;
import io.coti.common.data.DspVote;
import io.coti.common.data.TransactionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class IncomingCommunicationService {

    @Autowired
    private ZeroSpendTransactionService transactionService;
    @Autowired
    private IReceiver receiver;
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    @Autowired
    private AddressService addressService;
    @Autowired
    private DspVoteService dspVoteService;

    @PostConstruct
    private void init() {
        initDspVoteReceiver();
        initPropagationSubscriber();
    }

    private void initPropagationSubscriber() {
        HashMap<String, Consumer<Object>> classNameToSubscriberHandlerMapping = new HashMap<>();
        classNameToSubscriberHandlerMapping.put(TransactionData.class.getName() + "ZeroSpend Server", transactionData -> transactionService.handlePropagatedTransaction((TransactionData) transactionData));
        classNameToSubscriberHandlerMapping.put(AddressData.class.getName() + "ZeroSpend Server", data -> addressService.handlePropagatedAddress((AddressData) data));
        propagationSubscriber.init(classNameToSubscriberHandlerMapping);
    }

    private void initDspVoteReceiver() {
        HashMap<String, Function<Object, String>> voteMapping = new HashMap<>();
        voteMapping.put(DspVote.class.getName(), dspVote -> dspVoteService.receiveDspVote((DspVote) dspVote));
        receiver.init(voteMapping);
    }
}