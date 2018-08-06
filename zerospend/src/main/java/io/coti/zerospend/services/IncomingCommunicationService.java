package io.coti.zerospend.services;

import io.coti.common.communication.DspVote;
import io.coti.common.communication.interfaces.IPropagationSubscriber;
import io.coti.common.communication.interfaces.IReceiver;
import io.coti.common.data.AddressData;
import io.coti.common.data.TransactionData;
import io.coti.zerospend.services.interfaces.IAddressService;
import io.coti.zerospend.services.interfaces.IDspPropagationService;
import io.coti.zerospend.services.interfaces.IDspVoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class IncomingCommunicationService {

    @Autowired
    private IDspPropagationService dspPropagationService;

    @Autowired
    private IDspVoteService dspVoteService;

    @Autowired
    private IReceiver zeroMQTransactionReceiver;

    @Autowired
    private IPropagationSubscriber propagationSubscriber;

    @Autowired
    private IAddressService addressService;

    private Function<Object, String> newDspVoteFromDspHandler = dspVote -> {
        if (dspVote != null) {
            if (dspVote instanceof DspVote) {
                dspVoteService.insertVoteHelper((DspVote) dspVote);
                return "GOOD!";
            } else {
                return "BAD!";
            }
        } else {
            return "BAD!";
        }
    };

    private Consumer<Object> newTransactionFromDspHandler = transactionData -> {
        if (transactionData != null) {
            if (transactionData instanceof TransactionData) {
                dspPropagationService.handlePropagatedTransactionFromDSP((TransactionData) transactionData);
            } else {
                // TODO: Throw exception?
            }
        } else {
            // TODO: Throw exception?
        }
    };

    private Consumer<Object> newAddressFromDspHandler = addressData -> {
        if (addressData != null) {
            if (addressData instanceof AddressData) {
                addressService.handlePropagatedAddress((AddressData) addressData);
            } else {
                // TODO: Throw exception?
            }
        } else {
            // TODO: Throw exception?
        }
    };


    @PostConstruct
    private void init() {
        initDspVoteReceiver();
        initPropagationSubscriber();
    }

    private void initPropagationSubscriber() {
        HashMap<String, Consumer<Object>> messageHandler = new HashMap<>();
        messageHandler.put(TransactionData.class.getName() + "DSP Nodes", newTransactionFromDspHandler);
        messageHandler.put(AddressData.class.getName() + "DSP Nodes", newAddressFromDspHandler);
        propagationSubscriber.init(messageHandler);
    }

    private void initDspVoteReceiver() {
        HashMap<String, Function<Object, String>> voteMapping = new HashMap<>();
        voteMapping.put(DspVote.class.getName(), newDspVoteFromDspHandler);
        zeroMQTransactionReceiver.init(voteMapping);
    }

}
