package io.coti.zero_spend.http;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import lombok.Data;

@Data
public class AddTransactionRequest {

    private Hash cotiNodeHash;

    private TransactionData transactionData;

}
