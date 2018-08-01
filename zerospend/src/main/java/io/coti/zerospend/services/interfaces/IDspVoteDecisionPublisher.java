package io.coti.zerospend.services.interfaces;

import io.coti.common.data.TransactionData;

public interface IDspVoteDecisionPublisher {
    void publish(TransactionData transactionData);
}
