package io.coti.zerospend.services;

import io.coti.common.communication.interfaces.IPropagationPublisher;
import io.coti.common.data.TransactionData;
import io.coti.zerospend.services.interfaces.ITransactionPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionPublisher implements ITransactionPublisher {

    @Autowired
    private IPropagationPublisher TransactionPropagationPublisher;

    public void publish(TransactionData transactionData, String channelName) {
        TransactionPropagationPublisher.propagateTransaction(transactionData, channelName);
    }


}
