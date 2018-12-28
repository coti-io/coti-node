package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.ReceiverBaseTransactionData;
import lombok.Data;

@Data
public class ReceiverBaseTransactionResponseData extends OutputBaseTransactionResponseData {
    private String receiverDescription;

    public ReceiverBaseTransactionResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);

        ReceiverBaseTransactionData receiverBaseTransactionData = (ReceiverBaseTransactionData) baseTransactionData;
        this.receiverDescription = receiverBaseTransactionData.getReceiverDescription() != null ? receiverBaseTransactionData.getReceiverDescription().toString() : null;
    }
}
