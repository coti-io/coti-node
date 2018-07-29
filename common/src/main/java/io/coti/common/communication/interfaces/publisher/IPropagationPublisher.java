package io.coti.common.communication.interfaces.publisher;

import io.coti.common.data.AddressData;
import io.coti.common.data.TransactionData;

public interface IPropagationPublisher {

    void propagateAddress(AddressData addressData, String channel);

    void propagateTransaction(TransactionData transactionData, String channel);
}
