package io.coti.zero_spend.http;

import io.coti.common.data.TransactionData;
import lombok.Data;

@Data
public class AddTransactionRequest {


    private TransactionData transactionData;


}
