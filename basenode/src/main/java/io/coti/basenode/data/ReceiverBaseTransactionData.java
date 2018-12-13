package io.coti.basenode.data;

import lombok.Data;
import io.coti.basenode.data.interfaces.IReceiverBaseTransactionData;

@Data
public class ReceiverBaseTransactionData extends OutputBaseTransactionData implements IReceiverBaseTransactionData {

    private Hash receiverDescription;

    private ReceiverBaseTransactionData() {
        super();
    }

    public Hash getReceiverBaseTransactionAddress()
    {
        return addressHash;
    }

}
