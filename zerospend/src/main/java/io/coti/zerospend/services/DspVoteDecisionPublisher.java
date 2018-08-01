package io.coti.zerospend.services;

import io.coti.common.communication.interfaces.IPropagationPublisher;
import io.coti.common.data.TransactionData;
import io.coti.zerospend.services.interfaces.IDspVoteDecisionPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DspVoteDecisionPublisher implements IDspVoteDecisionPublisher {

    @Autowired
    private IPropagationPublisher TransactionPropagationPublisher;

    public void publish(TransactionData transactionData) {
        TransactionPropagationPublisher.propagateTransaction(transactionData, "Voting answer");
    }


}
