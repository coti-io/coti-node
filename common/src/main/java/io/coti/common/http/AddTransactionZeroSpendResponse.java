package io.coti.common.http;

import io.coti.common.data.TransactionData;
import lombok.Data;

@Data
public class AddTransactionZeroSpendResponse extends Response {
    private TransactionData transactionData;

}
