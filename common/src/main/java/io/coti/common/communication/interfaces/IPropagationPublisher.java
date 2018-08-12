package io.coti.common.communication.interfaces;

import io.coti.common.data.AddressData;
import io.coti.common.data.DspConsensusResult;
import io.coti.common.data.TransactionData;

public interface IPropagationPublisher {

    void propagateAddress(AddressData addressData, String channel);

    void propagateTransaction(TransactionData transactionData, String channel);

    void propagateDspResult(DspConsensusResult dspConsensusResult, String channel);
}
