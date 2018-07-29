package io.coti.common.communication.interfaces;

import io.coti.common.communication.DspVote;
import io.coti.common.data.AddressData;
import io.coti.common.data.TransactionData;

public interface ISerializer {

    byte[] serializeTransaction(TransactionData transactionData);

    TransactionData deserializeTransaction(byte[] bytes);

    byte[] serializeDspVote(DspVote dspVote);

    DspVote deserializeDspVote(byte[] bytes);

    byte[] serializeAddress(AddressData addressData);

    AddressData deserializeAddress(byte[] bytes);


}
