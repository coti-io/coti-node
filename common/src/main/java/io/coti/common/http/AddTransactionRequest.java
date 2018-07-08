package io.coti.common.http;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;

import javax.validation.constraints.NotNull;
import java.util.List;

public class AddTransactionRequest extends Request {
    @NotNull
    public List<BaseTransactionData> baseTransactions;
    @NotNull
    public Hash transactionHash;
    @NotNull
    public String message;

    public TransactionData transactionData;
}


