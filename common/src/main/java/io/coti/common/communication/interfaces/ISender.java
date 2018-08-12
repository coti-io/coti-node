package io.coti.common.communication.interfaces;

import io.coti.common.data.DspVote;
import io.coti.common.data.AddressData;
import io.coti.common.data.TransactionData;

public interface ISender {

    void sendTransaction(TransactionData transactionData);

    void sendAddress(AddressData addressData);

    void sendDspVote(DspVote dspVote);
}
