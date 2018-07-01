package io.coti.cotinode.http;

import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;

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
