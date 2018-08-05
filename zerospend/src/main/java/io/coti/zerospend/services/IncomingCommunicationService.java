package io.coti.zerospend.services;

import io.coti.common.communication.DspVote;
import io.coti.common.communication.interfaces.IPropagationSubscriber;
import io.coti.common.communication.interfaces.IReceiver;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.zerospend.DspCsvImporter;
import io.coti.zerospend.services.interfaces.IDspPropagationService;
import io.coti.zerospend.services.interfaces.IDspVoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
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

    private Function<Object, String> newDspVoteFromDsp = dspVote -> {
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

    private Consumer<Object> newTransactionFromDsp = transactionData -> {
        if (transactionData != null) {
            if (transactionData instanceof TransactionData) {
                dspPropagationService.acceptPropagation((TransactionData) transactionData);
            } else {
                // TODO: Throw exception?
            }
        } else {
            // TODO: Throw exception?
        }
    };

    @PostConstruct
    private void init() {
        initDspVoteService();
        initDspPropagationService();
    }

    private void initDspPropagationService() {
        HashMap<String, Consumer<Object>> messageHandler = new HashMap<>();
        messageHandler.put(TransactionData.class.getName() + "DSP Nodes", newTransactionFromDsp);
        propagationSubscriber.init(messageHandler);
    }

    private void initDspVoteService() {
        HashMap<String, Function<Object, String>> voteMapping = new HashMap<>();
        voteMapping.put(DspVote.class.getName(), newDspVoteFromDsp);
        zeroMQTransactionReceiver.init(voteMapping);
    }

}
