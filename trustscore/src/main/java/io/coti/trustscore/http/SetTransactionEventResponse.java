package io.coti.trustscore.http;


import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.data.enums.EventType;
import lombok.Data;

@Data
public class SetTransactionEventResponse extends BaseResponse {
    private String userHash;
    private int eventType;
    private TransactionData transactionData;

    public SetTransactionEventResponse(Hash userHash, EventType eventType, TransactionData transactionData) {
        this.userHash = userHash.toHexString();
        this.eventType = eventType.getValue();
        this.transactionData = transactionData;
    }

}