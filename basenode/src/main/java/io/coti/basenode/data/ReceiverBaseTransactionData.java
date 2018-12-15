package io.coti.basenode.data;

import lombok.Data;

@Data
public class ReceiverBaseTransactionData extends OutputBaseTransactionData {

    private Hash receiverDescription;

    private ReceiverBaseTransactionData() {
        super();
    }

}
