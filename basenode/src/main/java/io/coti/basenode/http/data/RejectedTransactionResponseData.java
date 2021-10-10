package io.coti.basenode.http.data;

import io.coti.basenode.data.RejectedTransactionData;
import io.coti.basenode.data.RejectedTransactionDataReason;
import io.coti.basenode.http.data.interfaces.ITransactionResponseData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@Data
public class RejectedTransactionResponseData implements ITransactionResponseData {

    private String hash;
    private Instant rejectionTime;
    private RejectedTransactionDataReason rejectionReason;

    protected RejectedTransactionResponseData() {

    }

    public RejectedTransactionResponseData(RejectedTransactionData rejectedTransaction) {
        this.hash = rejectedTransaction.getHash().toHexString();
        this.rejectionTime = rejectedTransaction.getRejectionTime();
        this.rejectionReason = rejectedTransaction.getRejectionReason();
    }
}
