package io.coti.basenode.http.data;

import io.coti.basenode.data.InvalidTransactionData;
import io.coti.basenode.data.InvalidTransactionDataReason;
import io.coti.basenode.http.data.interfaces.ITransactionResponseData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@Data
public class InvalidTransactionResponseData implements ITransactionResponseData {

    private String hash;
    private Instant invalidationTime;
    private InvalidTransactionDataReason invalidationReason;

    protected InvalidTransactionResponseData() {

    }

    public InvalidTransactionResponseData(InvalidTransactionData invalidTransaction) {
        this.hash = invalidTransaction.getHash().toHexString();
        this.invalidationTime = invalidTransaction.getInvalidationTime();
        this.invalidationReason = invalidTransaction.getInvalidationReason();
    }
}
