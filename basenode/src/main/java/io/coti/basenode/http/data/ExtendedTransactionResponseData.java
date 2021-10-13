package io.coti.basenode.http.data;

import io.coti.basenode.data.TransactionData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedTransactionResponseData extends TransactionResponseData {

    private String nodeHash;

    public ExtendedTransactionResponseData(TransactionData transactionData) {
        super(transactionData);
        this.nodeHash = transactionData.getNodeHash().toString();
    }

}
