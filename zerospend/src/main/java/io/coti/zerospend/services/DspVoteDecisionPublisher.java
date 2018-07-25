package io.coti.zerospend.services;

import io.coti.common.communication.interfaces.publisher.ITransactionPropagationPublisher;
import io.coti.common.data.TransactionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DspVoteDecisionPublisher {

    @Autowired
    private ITransactionPropagationPublisher TransactionPropagationPublisher;

    public void publish(TransactionData transactionData){
        TransactionPropagationPublisher.propagateTransactionToDSPs(transactionData);
    }



}
