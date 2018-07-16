package io.coti.common.http;

import io.coti.common.data.TransactionData;
import io.coti.common.http.Response;
import lombok.Data;

@Data
public class AddTransactionZeroSpendResponse extends Response {
    private TransactionData transactionData;

}
