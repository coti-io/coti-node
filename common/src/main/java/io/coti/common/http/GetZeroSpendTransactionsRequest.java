package io.coti.common.http;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import lombok.Data;

@Data
public class GetZeroSpendTransactionsRequest {


    private Hash fullNodeHash;

    private TransactionData transactionData;



}
