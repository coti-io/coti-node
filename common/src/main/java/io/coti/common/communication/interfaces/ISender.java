package io.coti.common.communication.interfaces;

import io.coti.common.communication.DspVote;
import io.coti.common.data.AddressData;
import io.coti.common.data.TransactionData;

public interface ISender {

    void sendTransactionToDsps(TransactionData transactionData);

    void sendAddress(AddressData addressData);

    void sendTransactionToZeroSpend(TransactionData transactionData);

    void sendDspVote(DspVote dspVote);
}
