package io.coti.basenode.http.data;

import io.coti.basenode.data.RejectedTransactionData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class RejectedTransactionResponseData extends ExtendedTransactionResponseData {

    private Instant rejectionTime;
    private String rejectionReasonDescription;

    public RejectedTransactionResponseData() {
        super();
    }

    public RejectedTransactionResponseData(RejectedTransactionData rejectedTransaction) {
        super(rejectedTransaction.getTransactionData());
        this.rejectionTime = rejectedTransaction.getRejectionTime();
        this.rejectionReasonDescription = rejectedTransaction.getRejectionReasonDescription();
    }
}
